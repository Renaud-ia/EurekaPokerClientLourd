package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ObjetClusterisable;

public class ClusteringEquilibrage extends ClusteringHierarchique {
    public ClusteringEquilibrage(MethodeLiaison methodeLiaison) {
        super(methodeLiaison);
    }


    public ObjetClusterisable[] dernierCluster() {
        return new ObjetClusterisable[3];
    }
}
