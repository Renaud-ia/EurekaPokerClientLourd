package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.cluster.DistanceCluster;
import analyzor.modele.clustering.cluster.ObjetClusterisable;
import analyzor.modele.clustering.liaison.StrategieFactory;
import analyzor.modele.clustering.liaison.StrategieLiaison;

import java.util.*;

public abstract class ClusteringHierarchique<T extends ObjetClusterisable> {
    public enum MethodeLiaison {
        COMPLETE, WARD, CENTREE, MEDIANE
    }
    private final StrategieLiaison strategieLiaison;
    private final List<ClusterHierarchique> clustersActuels;
    private final PriorityQueue<DistanceCluster> matriceDistances;
    // utilisé pour vérifier rapidement si la distance retenue correspond à des clusters encore actifs
    private final ArrayList<Boolean> clusterSupprime;
    // important le hashcode se base sur le numéro d'index
    private int indexActuel;
    public ClusteringHierarchique(MethodeLiaison methodeLiaison) {
        this.strategieLiaison = StrategieFactory.getStrategie(methodeLiaison);
        matriceDistances = new PriorityQueue<>(
                Comparator.comparingDouble(DistanceCluster::getDistance)
        );
        clustersActuels = new ArrayList<>();
        clusterSupprime = new ArrayList<>();
        indexActuel = 0;
    }

    private void initialiserMatrice() {
        for (int i = 0; i < clustersActuels.size(); i++) {
            for (int j = i + 1; j < clustersActuels.size(); j++) {
                ClusterHierarchique cluster1 = clustersActuels.get(i);
                ClusterHierarchique cluster2 = clustersActuels.get(j);

                float distance = strategieLiaison.calculerDistance(cluster1, cluster2);
                DistanceCluster distanceCluster = new DistanceCluster(cluster1, cluster2, distance);
                matriceDistances.add(distanceCluster);
            }
        }
    }

    boolean clusterSuivant() {
        if (clustersActuels.size() < 2) return false;
        boolean distanceInvalide = true;
        DistanceCluster distanceRetenue;
        ClusterHierarchique cluster1 = null;
        ClusterHierarchique cluster2 = null;
        while(distanceInvalide) {
            // on récupère la distance la plus courte
            distanceRetenue = matriceDistances.poll();
            cluster1 = Objects.requireNonNull(distanceRetenue).getPremierCluster();
            cluster2 = distanceRetenue.getSecondCluster();

            // si les deux clusters sont toujours actifs
            if (!clusterSupprime.get(cluster1.getIndex()) && !clusterSupprime.get(cluster2.getIndex())) {
                distanceInvalide = false;
            }
        }
        // on supprime les anciens clusters
        supprimerCluster(cluster1);
        supprimerCluster(cluster2);
        clusterSupprime.set(cluster1.getIndex(), true);
        clusterSupprime.set(cluster2.getIndex(), true);

        // on en crée un nouveau
        ClusterHierarchique nouveauCluster = new ClusterHierarchique(cluster1, cluster2, indexActuel++);

        // on calcule les distances avec tous les autres clusters
        for (ClusterHierarchique autreCluster : clustersActuels) {
            float distance = strategieLiaison.calculerDistance(cluster1, cluster2);
            DistanceCluster distanceCluster = new DistanceCluster(nouveauCluster, autreCluster, distance);
            matriceDistances.add(distanceCluster);
        }

        // on le rajoute dans les listes
        clustersActuels.add(nouveauCluster);
        clusterSupprime.add(true);

        // attention, il faut générer des clés pour les clusters qu'on crée, qui correspondent à une clé qui ne sera plus utilisée
        return true;
    }

    // méthode rapide pour supprimer un cluster de la liste (0(1)) plutôt que O(n)
    // on échange avec le dernier puis on supprime le dernier
    private void supprimerCluster(ClusterHierarchique clusterSupprime) {
        int lastIndex = clustersActuels.size() - 1;
        clustersActuels.set(clusterSupprime.getIndex(), clustersActuels.get(lastIndex));
        clustersActuels.remove(lastIndex);
    }
}
