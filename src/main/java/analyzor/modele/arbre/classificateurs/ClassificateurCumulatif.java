package analyzor.modele.arbre.classificateurs;

import analyzor.modele.arbre.noeuds.NoeudDenombrable;
import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;

import java.util.ArrayList;
import java.util.List;

public class ClassificateurCumulatif extends Classificateur {
    //todo

    @Override
    public List<NoeudDenombrable> obtenirSituations(List<Entree> entreesSituation) {
        // si aucune situation on retourne une liste vide
        // impossible en théorie -> à voir si utile
        if (!super.situationValide(entreesSituation)) return new ArrayList<>();

        List<NoeudDenombrable> listeNoeudsDenombrables = new ArrayList<>();

        // on clusterise par SPRB -> il faut récupérer les centroides
        List<ClusterSPRB> clustersSPRB = this.clusteriserSPRB(entreesSituation);

        for (ClusterSPRB clusterGroupe : clustersSPRB) {
            NoeudDenombrable noeudDenombrable = new NoeudDenombrable();
            for (Long idNoeudTheorique : clusterGroupe.noeudsPresents()) {
                List<Entree> entreesAction = clusterGroupe.obtenirEntrees(idNoeudTheorique);

                int minObservations = 400;
                // on prend les actions significatives -> fixer un seuil minimum d'observations
                if (entreesAction.size() < minObservations) continue;

                // sinon on crée un noeud
                NoeudPreflop noeudPreflop =
                        new NoeudPreflop(idNoeudTheorique, clusterGroupe.getEffectiveStack(),
                                clusterGroupe.getPot(), clusterGroupe.getPotBounty());

                // on clusterise les raises par bet size
                // on crée les noeuds actions et on les ajoute avec les entrées dans un noeud dénombrable
                if (noeudPreflop.getMove() == Move.RAISE) {
                    List<ClusterBetSize> clusterBetSizes = this.clusteriserBetSize(entreesAction);
                }
                else noeudDenombrable.ajouterNoeud(noeudPreflop, entreesAction);
            }
            listeNoeudsDenombrables.add(noeudDenombrable);
        }

        // todo que faire si data vraiment insuffisante ??

        return listeNoeudsDenombrables;
    }


}
