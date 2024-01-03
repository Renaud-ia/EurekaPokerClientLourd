package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.clustering.objets.EntreeBetSize;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public class KMeansBetSize extends ClusteringKMeans<EntreeBetSize> implements ClusteringEntreeMinEffectif {
    private final int maxBetSize;
    public KMeansBetSize(int maxBetSize) {
        super();
        this.maxBetSize = maxBetSize;
    }
    @Override
    public void ajouterDonnees(List<Entree> donneesEntrees) {
        List<EntreeBetSize> donneesTransformees = new ArrayList<>();
        for (Entree entree : donneesEntrees) {
            EntreeBetSize entreeBetSize = new EntreeBetSize(entree);
            donneesTransformees.add(entreeBetSize);
        }
        super.initialiser(donneesTransformees);
    }

    @Override
    public List<ClusterBetSize> construireClusters(int minimumPoints) {
        // todo trouver une meilleure méthode pour débruiter
        List<ClusterKMeans<EntreeBetSize>> clustersValides = new ArrayList<>();

        int maxClusters = 20;
        for (int nClusters = maxClusters; nClusters > 0; nClusters--) {
            super.ajusterClusters(nClusters);

            clustersValides.clear();
            for (ClusterKMeans<EntreeBetSize> cluster : this.getClusters()) {
                if (cluster.getEffectif() > minimumPoints) clustersValides.add(cluster);
            }

            if (clustersValides.size() >= 2 && clustersValides.size() <= minimumPoints) {
                break;
            }
        }

        List<ClusterBetSize> resultats = new ArrayList<>();

        // on décompresse les données
        for (ClusterKMeans<EntreeBetSize> clusterKMeans : clustersValides) {
            ClusterBetSize clusterBetSize = new ClusterBetSize();
            for (EntreeBetSize entreeSPRB : clusterKMeans.getObjets()) {
                clusterBetSize.ajouterEntree(entreeSPRB.getEntree());
            }
            clusterBetSize.setBetSizePlusFrequent();
            resultats.add(clusterBetSize);
        }

        return resultats;
    }
}
