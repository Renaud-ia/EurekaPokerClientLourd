package analyzor.modele.clustering.liaison;

import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.objets.ObjetClusterisable;

public abstract class StrategieLiaison<T extends ObjetClusterisable> {
    public abstract float calculerDistance(ClusterFusionnable<T> cluster1, ClusterFusionnable<T> cluster2);
}
