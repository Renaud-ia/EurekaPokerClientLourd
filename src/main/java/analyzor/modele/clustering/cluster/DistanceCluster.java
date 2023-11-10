package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.utils.Bits;

public class DistanceCluster<T extends ObjetClusterisable> {
    private long index;
    private ClusterHierarchique<T> cluster1;
    private ClusterHierarchique<T> cluster2;
    private float distance;

    public DistanceCluster(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2,
                           float distance, long index) {
        if (cluster1 == null || cluster2 == null) {
            throw new IllegalArgumentException("Un des clusters est nul");
        }
        this.index = index;

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

    public long getIndex() {
        return index;
    }

    public void setPremierCluster(ClusterHierarchique<T> clusterFusionne) {
        this.cluster1 = clusterFusionne;
    }

    public void setSecondCluster(ClusterHierarchique<T> clusterFusionne) {
        this.cluster2 = clusterFusionne;
    }

    public boolean contient(ClusterHierarchique<T> clusterSupprime) {
        return cluster1.equals(clusterSupprime) || cluster2.equals(clusterSupprime);
    }

    public void setDistance(float nouvelleDistance) {
        this.distance = nouvelleDistance;
    }
}
