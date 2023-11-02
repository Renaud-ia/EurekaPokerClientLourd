package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

public abstract class StrategieLiaison<T extends ObjetClusterisable> {
    public abstract float calculerDistance(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2);
}
