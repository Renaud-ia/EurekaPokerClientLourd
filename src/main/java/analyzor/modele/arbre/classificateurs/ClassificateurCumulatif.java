package analyzor.modele.arbre.classificateurs;

import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.arbre.RecupRangeIso;
import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.denombrement.NoeudDenombrableIso;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.evaluation.EquiteFuture;
import analyzor.modele.poker.evaluation.OppositionRange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
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
    private final static int MIN_ECHANTILLON = 500;
    private final static float MIN_FREQUENCE_ACTION = 0.01f;
    private static final float MIN_FREQUENCE_BET_SIZE = 0.10f;
    private final static int MIN_EFFECTIF_BET_SIZE = 200;

    // variables associés à l'instance
    private final Random random;
    private final FormatSolution formatSolution;
    private final List<NoeudDenombrable> noeudDenombrables;
    private final ArbreAbstrait arbreAbstrait;

    public ClassificateurCumulatif(FormatSolution formatSolution) {
        this.random = new Random();
        this.formatSolution = formatSolution;
        this.noeudDenombrables = new ArrayList<>();
        this.arbreAbstrait = new ArbreAbstrait(formatSolution);
    }

    @Override
    public void creerSituations(List<Entree> entreesSituation) {
        // si aucune situation on retourne une liste vide
        // impossible en théorie -> à voir si utile
        if (!super.situationValide(entreesSituation)) return;

        // si pas assez de situations, on passe => à gérer par la suite
        if (entreesSituation.size() < MIN_ECHANTILLON) return;

        List<ClusterSPRB> clustersSPRB = this.clusteriserSPRB(entreesSituation, MIN_POINTS);


        for (ClusterSPRB clusterGroupe : clustersSPRB) {
            NoeudAbstrait premierNoeud = new NoeudAbstrait(clusterGroupe.getIdPremierNoeud());
            NoeudAbstrait noeudPrecedent = arbreAbstrait.noeudPrecedent(premierNoeud);

            NoeudDenombrableIso noeudDenombrable = new NoeudDenombrableIso(noeudPrecedent.stringReduite());
            logger.debug("#### STACK EFFECTIF #### : " + clusterGroupe.getEffectiveStack());

            // les clusters sont sous-groupés par action
            for (Long idNoeudTheorique : clusterGroupe.noeudsPresents()) {
                List<Entree> entreesAction = clusterGroupe.obtenirEntrees(idNoeudTheorique);

                // on vérifie si l'action est assez fréquente
                float frequenceAction = (float) entreesAction.size() / clusterGroupe.getEffectif();
                if (frequenceAction < MIN_FREQUENCE_ACTION) continue;


                NoeudAbstrait noeudAbstraitAction = new NoeudAbstrait(idNoeudTheorique);
                logger.debug("Noeud abstrait : " + noeudAbstraitAction);
                logger.debug("Fréquence de l'action " + frequenceAction);
                logger.debug("Effectif  :" + entreesAction.size());

                if (noeudAbstraitAction.getMove() == Move.RAISE) {
                    // on clusterise les raises par bet size
                    creerNoeudParBetSize(entreesAction, clusterGroupe, idNoeudTheorique, noeudDenombrable);
                }
                else {
                    creerNoeudSansBetSize(entreesAction, clusterGroupe, idNoeudTheorique,
                            noeudAbstraitAction.getMove(), noeudDenombrable);
                }

            }
            noeudDenombrables.add(noeudDenombrable);
        }
    }

    @Override
    public void construireCombosDenombrables() {
        for (NoeudDenombrable noeudDenombrable : noeudDenombrables) {
            List<Entree> echantillon = noeudDenombrable.obtenirEchantillon();
            RecupRangeIso recuperateurRange = new RecupRangeIso(formatSolution);
            OppositionRange oppositionRange = recuperateurRange.recupererRanges(echantillon);
            ((NoeudDenombrableIso) noeudDenombrable).construireCombosPreflop(oppositionRange);
        }
    }

    @Override
    public List<NoeudDenombrable> obtenirSituations() {
        return noeudDenombrables;
    }

    /**
     * clusterise par BetSize et crée les noeuds
     */
    private void creerNoeudParBetSize(List<Entree> entreesAction, ClusterSPRB clusterGroupe,
                                      long idNoeudTheorique, NoeudDenombrable noeudDenombrable) {
        int minEffectifCluster =
                (int) Math.max(MIN_EFFECTIF_BET_SIZE, entreesAction.size() * MIN_FREQUENCE_BET_SIZE);
        List<ClusterBetSize> clustersSizing = this.clusteriserBetSize(entreesAction, minEffectifCluster);

        for (ClusterBetSize clusterBetSize : clustersSizing) {
            // si le betSize est supérieure à 70% stack effectif c'est comme all-in
            float fractSizeAllIn = 0.7f;
            if ((clusterBetSize.getBetSize() * clusterGroupe.getPot())
                    > (clusterGroupe.getEffectiveStack() * fractSizeAllIn)) continue;

            // on crée les noeuds actions et on les ajoute avec les entrées dans un noeud dénombrable
            NoeudPreflop noeudPreflop =
                    new NoeudPreflop(formatSolution, idNoeudTheorique, clusterGroupe.getEffectiveStack(),
                            clusterGroupe.getPot(), clusterGroupe.getPotBounty());
            noeudPreflop.setBetSize(clusterBetSize.getBetSize());
            noeudDenombrable.ajouterNoeud(noeudPreflop, clusterBetSize.getEntrees());

            logger.debug("BETSIZE : " + clusterBetSize.getBetSize());
            logger.debug("EFFECTIF : " + clusterBetSize.getEffectif());
        }
    }

    private float moyenneBetSize(List<Entree> entreesAction) {
        float sommeBetSize = 0;
        for (Entree entree : entreesAction) {
            sommeBetSize += entree.getBetSize();
        }

        return sommeBetSize / entreesAction.size();
    }

    /**
     * créer les noeuds sans betSize
     */
    private void creerNoeudSansBetSize(List<Entree> entreesAction, ClusterSPRB clusterGroupe,
                                       long idNoeudTheorique, Move move, NoeudDenombrable noeudDenombrable) {
        // on crée les noeuds actions et on les ajoute avec les entrées dans un noeud dénombrable
        NoeudPreflop noeudPreflop =
                new NoeudPreflop(formatSolution, idNoeudTheorique, clusterGroupe.getEffectiveStack(),
                        clusterGroupe.getPot(), clusterGroupe.getPotBounty());

        if (move == Move.ALL_IN)
            noeudPreflop.setBetSize(clusterGroupe.getPot());
        // sinon on crée un noeud
        noeudDenombrable.ajouterNoeud(noeudPreflop, entreesAction);
    }

}
