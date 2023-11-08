package analyzor.modele.arbre.classificateurs;

import analyzor.modele.clustering.ClusteringHierarchicalSPRB;
import analyzor.modele.clustering.ClusteringSPRB;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public abstract class Classificateur implements CreerLabel, RetrouverLabel {

    List<ClusterSPRB> clusteriserSPRB(List<Entree> entrees) {
        // todo fixer une valeur
        int minimumPoints = 1000;
        ClusteringSPRB clusteringSPRB = new ClusteringHierarchicalSPRB();
        clusteringSPRB.ajouterDonnees(entrees);

        return clusteringSPRB.construireClusters(minimumPoints);
    }

    /**
     * procédure de vérification
     * @param entreesSituation
     * @return
     */
    protected boolean situationValide(List<Entree> entreesSituation) {
        //todo ajouter un nombre minimum de mains
        if (entreesSituation.isEmpty()) return false;
        else return true;
    }

    protected List<ClusterBetSize> clusteriserBetSize(List<Entree> entreesAction) {
        return new ArrayList<>();
    }
}
