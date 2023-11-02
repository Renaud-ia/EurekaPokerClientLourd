package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

public class DistanceCluster<T extends ObjetClusterisable> {
    private ClusterHierarchique<T> cluster1;
    private ClusterHierarchique<T> cluster2;
    private final float distance;

    public DistanceCluster(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2, float distance) {
        if (cluster1 == null || cluster2 == null) {
            throw new IllegalArgumentException("Un des clusters est nul");
        }

        this.cluster1 = cluster1;
        this.cluster2 = cluster2;
        this.distance = distance;
    }

    public float getDistance() {
        return distance;
    }

    public ClusterHierarchique<T> getPremierCluster() {
        return cluster1;
    }

    public ClusterHierarchique<T> getSecondCluster() {
        return cluster2;
    }
}
