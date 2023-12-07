package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;

public class ClusterFusionnable<T extends ObjetClusterisable> extends BaseCluster<T> {
    int index;

    public ClusterFusionnable(T objetDepart, int indexCluster) {
        super();
        listeObjets.add(objetDepart);
        index = indexCluster;
    }

    public ClusterFusionnable(ClusterFusionnable<T> cluster1, ClusterFusionnable<T> cluster2, int indexCluster) {
        if (cluster1 == null || cluster2 == null) {
            throw new IllegalArgumentException("Un des clusters est null");
        }
        this.listeObjets = new ArrayList<>();
        this.listeObjets.addAll(cluster1.getObjets());
        this.listeObjets.addAll(cluster2.getObjets());
        this.index = indexCluster;
    }

    public ClusterFusionnable(ClusterKMeans<T> clusterKMeans, int indexCluster) {
        this.listeObjets = clusterKMeans.getObjets();
        this.index = indexCluster;
    }

    public int getIndex() {
        return index;
    }

    public void fusionner(ClusterFusionnable<T> clusterInitial) {
        if (clusterInitial == this) return;
        this.listeObjets.addAll(clusterInitial.getObjets());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClusterFusionnable)) return false;
        else return this.index == ((ClusterFusionnable<?>) o).getIndex();
    }
}
