package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.objets.ObjetClusterisable;

public class ClusteringEquilibrage extends ClusteringHierarchique {
    public ClusteringEquilibrage(MethodeLiaison methodeLiaison) {
        super(methodeLiaison);
    }


    public ObjetClusterisable[] dernierCluster() {
        return new ObjetClusterisable[3];
    }
}
