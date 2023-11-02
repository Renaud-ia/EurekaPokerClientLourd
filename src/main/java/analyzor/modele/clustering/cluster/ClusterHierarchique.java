package analyzor.modele.clustering.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClusterHierarchique {
    List<ObjetClusterisable> listeObjets;
    int index;

    public ClusterHierarchique(List<ObjetClusterisable> objetDepart, int indexCluster) {
        listeObjets = objetDepart;
        index = indexCluster;
    }

    public ClusterHierarchique(ClusterHierarchique cluster1, ClusterHierarchique cluster2, int indexCluster) {
        if (cluster1 == null || cluster2 == null) {
            throw new IllegalArgumentException("Un des clusters est null");
        }
        this.listeObjets = new ArrayList<>();
        this.listeObjets.addAll(cluster1.getObjets());
        this.listeObjets.addAll(cluster2.getObjets());
        this.index = indexCluster;
    }

    private List<ObjetClusterisable> getObjets() {
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
}
