package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;

public class ClusterHierarchique<T extends ObjetClusterisable> extends BaseCluster<T> {
    int index;

    public ClusterHierarchique(T objetDepart, int indexCluster) {
        listeObjets = new ArrayList<>();
        listeObjets.add(objetDepart);
        index = indexCluster;
    }

    public ClusterHierarchique(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2, int indexCluster) {
        if (cluster1 == null || cluster2 == null) {
            throw new IllegalArgumentException("Un des clusters est null");
        }
        this.listeObjets = new ArrayList<>();
        this.listeObjets.addAll(cluster1.getObjets());
        this.listeObjets.addAll(cluster2.getObjets());
        this.index = indexCluster;
    }

    public int getIndex() {
        return index;
    }

    public void fusionner(ClusterHierarchique<T> clusterInitial) {
        if (clusterInitial == this) return;
        this.listeObjets.addAll(clusterInitial.getObjets());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClusterHierarchique)) return false;
        else return this.index == ((ClusterHierarchique<?>) o).getIndex();
    }
}
