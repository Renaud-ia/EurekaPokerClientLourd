package analyzor.modele.clustering.cluster;

public abstract class StrategieLiaison {
    public abstract float calculerDistance(ClusterHierarchique cluster1, ClusterHierarchique cluster2);
}
