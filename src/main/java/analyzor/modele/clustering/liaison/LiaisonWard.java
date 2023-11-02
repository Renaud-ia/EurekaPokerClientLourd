package analyzor.modele.clustering.liaison;

import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.StrategieLiaison;

class LiaisonWard extends StrategieLiaison {
    @Override
    public float calculerDistance(ClusterHierarchique cluster1, ClusterHierarchique cluster2) {
        return 0;
    }
}
