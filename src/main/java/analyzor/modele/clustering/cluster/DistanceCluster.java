package analyzor.modele.clustering.cluster;

public class DistanceCluster {
    private ClusterHierarchique cluster1;
    private ClusterHierarchique cluster2;
    private float distance;

    public DistanceCluster(ClusterHierarchique cluster1, ClusterHierarchique cluster2, float distance) {
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

    public ClusterHierarchique getPremierCluster() {
        return cluster1;
    }

    public ClusterHierarchique getSecondCluster() {
        return cluster2;
    }
}
