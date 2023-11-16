package analyzor.modele.arbre.classificateurs;

import analyzor.modele.arbre.NoeudDenombrable;
import analyzor.modele.arbre.RecupRangeIso;
import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.evaluation.OppositionRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * un classificateur est crée pour chaque noeud abstrait précédent
 */
public class ClassificateurCumulatif extends Classificateur {
    /**
     * @param entreesSituation entrées correspondant à un même noeud abstrait précédent
     * @param formatSolution
     * @return des noeuds dénombrables
     */
    // valeurs config
    // on fixe minPoints ici car dépend du round
    private final static int MIN_POINTS = 1200;
    private final static int N_ECHANTILLONS = 5;
    private final static float MIN_FREQUENCE_ACTION = 0.01f;
    private static final float MIN_FREQUENCE_BET_SIZE = 0.25f;
    private final static int MIN_EFFECTIF_BET_SIZE = (int) (MIN_POINTS * 0.3f * MIN_FREQUENCE_BET_SIZE);

    // variables associés à l'instance
    private int nEchantillonParLoop;
    private final List<Entree> echantillon;
    private final Random random;
    private final FormatSolution formatSolution;
    private final NoeudDenombrable noeudDenombrable;

    public ClassificateurCumulatif(FormatSolution formatSolution) {
        this.echantillon = new ArrayList<>();
        this.random = new Random();
        this.formatSolution = formatSolution;
        this.noeudDenombrable = new NoeudDenombrable();
    }

    @Override
    public List<NoeudDenombrable> obtenirSituations(List<Entree> entreesSituation) {
        // si aucune situation on retourne une liste vide
        // impossible en théorie -> à voir si utile
        if (!super.situationValide(entreesSituation)) return new ArrayList<>();

        List<NoeudDenombrable> listeNoeudsDenombrables = new ArrayList<>();

        List<ClusterSPRB> clustersSPRB = this.clusteriserSPRB(entreesSituation, MIN_POINTS);
        this.nEchantillonParLoop = N_ECHANTILLONS / clustersSPRB.size();
        if (nEchantillonParLoop == 0) nEchantillonParLoop = 1;

        for (ClusterSPRB clusterGroupe : clustersSPRB) {
            System.out.println("#### STACK EFFECTIF #### : " + clusterGroupe.getEffectiveStack());

            // les clusters sont sous-groupés par action
            for (Long idNoeudTheorique : clusterGroupe.noeudsPresents()) {
                List<Entree> entreesAction = clusterGroupe.obtenirEntrees(idNoeudTheorique);

                // on vérifie si l'action est assez fréquente
                float frequenceAction = (float) entreesAction.size() / clusterGroupe.getEffectif();
                if (frequenceAction < MIN_FREQUENCE_ACTION) continue;

                recupererEchantillon(entreesAction);

                NoeudAbstrait noeudAbstraitAction = new NoeudAbstrait(idNoeudTheorique);
                System.out.println("Noeud abstrait : " + noeudAbstraitAction);
                System.out.println("Fréquence de l'action " + frequenceAction);
                System.out.println("Effectif  :" + entreesAction.size());

                if (noeudAbstraitAction.getMove() == Move.RAISE) {
                    // on clusterise les raises par bet size
                    creerNoeudParBetSize(entreesAction, clusterGroupe, idNoeudTheorique);
                }
                else {
                    creerNoeudSansBetSize(entreesAction, clusterGroupe, idNoeudTheorique, noeudAbstraitAction.getMove());
                }

            }
            OppositionRange oppositionRange = obtenirRanges();
            noeudDenombrable.construireCombosPreflop(oppositionRange);

            listeNoeudsDenombrables.add(noeudDenombrable);
        }

        // todo que faire si data vraiment insuffisante ??

        return listeNoeudsDenombrables;
    }

    private void recupererEchantillon(List<Entree> entreesAction) {
        // on prend des échantillons random
        for (int i = 0; i <= nEchantillonParLoop; i++) {
            int randomEchantillon = random.nextInt(entreesAction.size());
            this.echantillon.add(entreesAction.get(randomEchantillon));
        }
    }

    /**
     * clusterise par BetSize et crée les noeuds
     */
    private void creerNoeudParBetSize(List<Entree> entreesAction, ClusterSPRB clusterGroupe, long idNoeudTheorique) {
        int minEffectifCluster =
                (int) Math.max(MIN_EFFECTIF_BET_SIZE, entreesAction.size() * MIN_FREQUENCE_BET_SIZE);
        List<ClusterBetSize> clustersSizing = this.clusteriserBetSize(entreesAction, minEffectifCluster);
        for (ClusterBetSize clusterBetSize : clustersSizing) {
            // si le betSize est supérieure au stack effectif c'est comme all-in
            if (clusterBetSize.getBetSize() > clusterGroupe.getEffectiveStack()) continue;

            // on crée les noeuds actions et on les ajoute avec les entrées dans un noeud dénombrable
            NoeudPreflop noeudPreflop =
                    new NoeudPreflop(formatSolution, idNoeudTheorique, clusterGroupe.getEffectiveStack(),
                            clusterGroupe.getPot(), clusterGroupe.getPotBounty());
            noeudPreflop.setBetSize(clusterBetSize.getBetSize());
            noeudDenombrable.ajouterNoeud(noeudPreflop, entreesAction);

            System.out.println("BETSIZE : " + clusterBetSize.getBetSize());
        }
    }

    /**
     * créer les noeuds sans betSize
     */
    private void creerNoeudSansBetSize(List<Entree> entreesAction, ClusterSPRB clusterGroupe, long idNoeudTheorique, Move move) {
        // on crée les noeuds actions et on les ajoute avec les entrées dans un noeud dénombrable
        NoeudPreflop noeudPreflop =
                new NoeudPreflop(formatSolution, idNoeudTheorique, clusterGroupe.getEffectiveStack(),
                        clusterGroupe.getPot(), clusterGroupe.getPotBounty());

        if (move == Move.ALL_IN)
            noeudPreflop.setBetSize(clusterGroupe.getEffectiveStack());
        // sinon on crée un noeud
        noeudDenombrable.ajouterNoeud(noeudPreflop, entreesAction);
    }

    private OppositionRange obtenirRanges() {
        RecupRangeIso recuperateurRange = new RecupRangeIso(formatSolution);

        return recuperateurRange.recupererRanges(this.echantillon);
    }

}
