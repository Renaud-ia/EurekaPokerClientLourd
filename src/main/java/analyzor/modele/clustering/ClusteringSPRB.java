package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.clustering.objets.EntreeSPRB;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

/**
 * clustering Hierarchique selon Effective stack, Pot et pot Bounty
 */
public class ClusteringSPRB extends ClusteringHierarchique<EntreeSPRB> {

    public ClusteringSPRB() {
        super(MethodeLiaison.WARD);
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

    public List<ClusterSPRB> construireClusters(int minimumPoints) {
        List<ClusterSPRB> resultats = new ArrayList<>();

        Integer minEffectif = clusterSuivant();
        if (minEffectif == null) return null;

        while(minEffectif < minimumPoints) {
            minEffectif = clusterSuivant();
            if (minEffectif == null) return null;
        }

        // on décompresse les clusters pour obtenir les résultats
        // les clusters sont sous-groupés par NoeudThéorique = action choisie
        for (ClusterHierarchique<EntreeSPRB> clusterHierarchique : clustersActuels) {
            ClusterSPRB clusterSPRB = new ClusterSPRB();
            for (EntreeSPRB entreeSPRB : clusterHierarchique.getObjets()) {
                clusterSPRB.ajouterEntree(entreeSPRB.getEntree());
            }
            clusterSPRB.setStackEffectif(clusterHierarchique.getCentroide()[0]);
            clusterSPRB.setPot(clusterHierarchique.getCentroide()[1]);
            clusterSPRB.setPotBounty(clusterHierarchique.getCentroide()[2]);
            resultats.add(clusterSPRB);
        }

        return resultats;
    }
}
