package analyzor.modele.clustering;


import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.clustering.objets.EntreeSPRB;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public class KMeansSPRB extends ClusteringKMeans<EntreeSPRB> implements ClusteringEntreeMinEffectif {
    public KMeansSPRB() {
        super();

    }
    @Override
    public void ajouterDonnees(List<Entree> donneesEntrees) {
        List<EntreeSPRB> donneesTransformees = new ArrayList<>();
        for (Entree entree : donneesEntrees) {
            EntreeSPRB entreeSPRB = new EntreeSPRB(entree);
            donneesTransformees.add(entreeSPRB);
        }
        super.initialiser(donneesTransformees);
    }

    @Override
    public List<ClusterSPRB> construireClusters(int minimumPoints) {
        List<ClusterKMeans<EntreeSPRB>> resultatValide = null;
        int nombreClusters = 1;

        // on augmente le nombre de clusters tant que minimumPoints est satisfait
        int count = 0;
        while(count++ < 100) {
            super.ajusterClusters(nombreClusters++);
            List<ClusterKMeans<EntreeSPRB>> clusters = super.getClusters();

            int minimumEffectif = minimumPoints;
            for (ClusterKMeans<EntreeSPRB> cluster : clusters) {
                if (cluster.getEffectif() < minimumEffectif) {
                    minimumEffectif = cluster.getEffectif();
                }
            }
            if (minimumEffectif < minimumPoints && count > 1) {
                break;
            }

            resultatValide = new ArrayList<>(clusters);
        }

        List<ClusterSPRB> resultats = new ArrayList<>();
        if (resultatValide == null) return resultats;

        // on décompresse les données
        for (ClusterKMeans<EntreeSPRB> clusterHierarchique : resultatValide) {
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
