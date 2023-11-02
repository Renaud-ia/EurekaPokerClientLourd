package analyzor.modele.clustering.liaison;

import analyzor.modele.clustering.cluster.ClusterHierarchique;

public abstract class StrategieLiaison {
    public abstract float calculerDistance(ClusterHierarchique cluster1, ClusterHierarchique cluster2);
}
