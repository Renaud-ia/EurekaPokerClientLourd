package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.objets.EntreeSPRB;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

/**
 * clustering Hierarchique selon Effective stack, Pot et pot Bounty
 */
public class ClusteringHierarchiqueSPRB extends ClusteringHierarchique<EntreeSPRB> {

    public ClusteringHierarchiqueSPRB(ClusteringHierarchique.MethodeLiaison methodeLiaison) {
        super(methodeLiaison);
    }

    public void ajouterDonnees(List<Entree> donneesEntrees) {
        for (Entree entree : donneesEntrees) {
            EntreeSPRB entreeSPRB = new EntreeSPRB(entree);
            ClusterHierarchique<EntreeSPRB> nouveauCluster = new ClusterHierarchique<>(entreeSPRB, indexActuel++);
            clustersActuels.add(nouveauCluster);
            clusterSupprime.put(nouveauCluster.getIndex(), false);
        }
        preClustering();
        initialiserMatrice();
    }

    public void preClustering() {}

    public List<List<Entree>> construireClusters(int minimumPoints) {
        List<List<Entree>> resultats = new ArrayList<>();

        Integer minEffectif = clusterSuivant();
        if (minEffectif == null) return null;

        while(minEffectif < minimumPoints) {
            minEffectif = clusterSuivant();
            if (minEffectif == null) return null;
        }

        // on décompresse les clusters pour obtenir les résultats
        for (ClusterHierarchique<EntreeSPRB> clusterHierarchique : clustersActuels) {
            List<Entree> objets = new ArrayList<>();
            for (EntreeSPRB entreeSPRB : clusterHierarchique.getObjets()) {
                objets.add(entreeSPRB.getEntree());
            }
            resultats.add(objets);
        }

        return resultats;
    }
}
