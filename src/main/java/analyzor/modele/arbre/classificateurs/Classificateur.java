package analyzor.modele.arbre.classificateurs;

import analyzor.modele.clustering.ClusteringEntreeMinEffectif;
import analyzor.modele.clustering.HierarchiqueSPRB;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public abstract class Classificateur implements CreerLabel, RetrouverLabel {

    List<ClusterSPRB> clusteriserSPRB(List<Entree> entrees) {
        // todo fixer une valeur
        int minimumPoints = 1000;
        ClusteringEntreeMinEffectif clusteringEntreeMinEffectif = new HierarchiqueSPRB();
        clusteringEntreeMinEffectif.ajouterDonnees(entrees);

        @SuppressWarnings("unchecked")
        List<ClusterSPRB> resultats = (List<ClusterSPRB>) clusteringEntreeMinEffectif.construireClusters(minimumPoints);

        return resultats;
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
