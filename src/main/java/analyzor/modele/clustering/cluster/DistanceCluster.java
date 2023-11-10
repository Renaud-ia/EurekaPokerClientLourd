package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.utils.Bits;

public class DistanceCluster<T extends ObjetClusterisable> {
    private long index;
    private ClusterHierarchique<T> cluster1;
    private ClusterHierarchique<T> cluster2;
    private final float distance;

    public DistanceCluster(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2, float distance) {
        if (cluster1 == null || cluster2 == null) {
            throw new IllegalArgumentException("Un des clusters est nul");
        }
        int bitsNecessaires = Bits.bitsNecessaires(cluster1.getIndex()) + Bits.bitsNecessaires(cluster2.getIndex());
        if (bitsNecessaires >= 63) throw new IllegalArgumentException("Les index sont trop grands : trop de valeurs initiales");
        this.index = ((long) cluster1.getIndex() << 32) | cluster2.getIndex();

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
}
