package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClusterHierarchique<T extends ObjetClusterisable> {
    List<T> listeObjets;
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

    public List<T> getObjets() {
        return listeObjets;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClusterHierarchique cluster = (ClusterHierarchique) obj;
        return index == cluster.index;
    }

    public int getIndex() {
        return index;
    }

    public int getEffectif() {
        return listeObjets.size();
    }
}
