package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.objets.EntreeSPRB;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

/**
 * clustering Hierarchique selon Effective SPR et pot Bounty
 */
public class ClusteringSPRB extends ClusteringHierarchique<EntreeSPRB> {

    public ClusteringSPRB(ClusteringHierarchique.MethodeLiaison methodeLiaison) {
        super(methodeLiaison);
    }

    public void ajouterDonnees(List<Entree> donneesEntrees) {
        for (Entree entree : donneesEntrees) {
            EntreeSPRB entreeSPRB = new EntreeSPRB(entree);
            ClusterHierarchique<EntreeSPRB> nouveauCluster = new ClusterHierarchique<>(entreeSPRB, indexActuel++);
            clustersActuels.add(nouveauCluster);
            clusterSupprime.put(nouveauCluster.getIndex(), false);
        }
        initialiserMatrice();
    }

    public List<List<Entree>> construireClusters(int minimumCluster) {
        List<List<Entree>> resultats = new ArrayList<>();

        Integer minEffectif = clusterSuivant();
        if (minEffectif == null) return null;

        while(minEffectif < minimumCluster) {
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
