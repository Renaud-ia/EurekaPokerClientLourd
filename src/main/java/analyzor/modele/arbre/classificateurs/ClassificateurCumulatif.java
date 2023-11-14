package analyzor.modele.arbre.classificateurs;

import analyzor.modele.arbre.RecuperateurRange;
import analyzor.modele.equilibrage.NoeudDenombrable;
import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClassificateurCumulatif extends Classificateur {
    /**
     * @param entreesSituation entrées correspondant à un même noeud abstrait précédent
     * @param formatSolution
     * @return des noeuds dénombrables
     */
    @Override
    public List<NoeudDenombrable> obtenirSituations(List<Entree> entreesSituation, FormatSolution formatSolution) {
        // valeurs config
        // on fixe minPoints ici car dépend du round
        int minPoints = 1200;
        int nEchantillons = 4;
        float minFrequenceAction = 0.01f;
        float minFrequenceBetSize = 0.25f;
        int minEffectifBetSize = (int) (minPoints * 0.3f * minFrequenceBetSize);

        // si aucune situation on retourne une liste vide
        // impossible en théorie -> à voir si utile
        if (!super.situationValide(entreesSituation)) return new ArrayList<>();

        List<NoeudDenombrable> listeNoeudsDenombrables = new ArrayList<>();
        Random random = new Random();

        List<ClusterSPRB> clustersSPRB = this.clusteriserSPRB(entreesSituation, minPoints);
        int nEchantillonParLoop = (int) nEchantillons / clustersSPRB.size();
        if (nEchantillonParLoop == 0) nEchantillonParLoop = 1;

        for (ClusterSPRB clusterGroupe : clustersSPRB) {
            NoeudDenombrable noeudDenombrable = new NoeudDenombrable();
            List<Entree> echantillonEntrees = new ArrayList<>();

            // les clusters sont sous-groupés par action
            for (Long idNoeudTheorique : clusterGroupe.noeudsPresents()) {
                List<Entree> entreesAction = clusterGroupe.obtenirEntrees(idNoeudTheorique);

                // on prend des échantillons random
                for (int i = 0; i <= nEchantillonParLoop; i++) {
                    int randomEchantillon = random.nextInt(entreesAction.size());
                    echantillonEntrees.add(entreesAction.get(randomEchantillon));
                }

                float frequenceAction = (float) entreesAction.size() / clusterGroupe.getEffectif();
                // on prend les actions significatives
                if (frequenceAction < minFrequenceAction) continue;


                // on crée les noeuds actions et on les ajoute avec les entrées dans un noeud dénombrable
                NoeudPreflop noeudPreflop =
                        new NoeudPreflop(formatSolution, idNoeudTheorique, clusterGroupe.getEffectiveStack(),
                                clusterGroupe.getPot(), clusterGroupe.getPotBounty());

                // on clusterise les raises par bet size
                NoeudAbstrait noeudAbstraitAction = new NoeudAbstrait(idNoeudTheorique);
                if (noeudAbstraitAction.getMove() == Move.RAISE) {
                    List<ClusterBetSize> clustersSizing = this.clusteriserBetSize(entreesAction, minEffectifBetSize);
                    for (ClusterBetSize clusterBetSize : clustersSizing) {
                        noeudPreflop.setBetSize(clusterBetSize.getBetSize());
                    }
                }
                else {
                    // sinon on crée un noeud
                    noeudDenombrable.ajouterNoeud(noeudPreflop, entreesAction);
                }

            }
            RecuperateurRange recuperateurRange = new RecuperateurRange(echantillonEntrees);
            noeudDenombrable.ajouterRanges(recuperateurRange);
            listeNoeudsDenombrables.add(noeudDenombrable);
        }

        // todo que faire si data vraiment insuffisante ??

        return listeNoeudsDenombrables;
    }


}
