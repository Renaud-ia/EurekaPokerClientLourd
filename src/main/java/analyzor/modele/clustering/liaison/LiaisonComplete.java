package analyzor.modele.clustering.liaison;

import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.objets.ObjetClusterisable;


public class LiaisonComplete<T extends ObjetClusterisable> extends StrategieLiaison<T> {
    @Override
    public float calculerDistance(ClusterFusionnable<T> cluster1, ClusterFusionnable<T> cluster2) {
        float maxDistance = 0;
        for (ObjetClusterisable objet1 : cluster1.getObjets()) {
            for (ObjetClusterisable objet2 : cluster2.getObjets()) {
                float distance = objet1.distance(objet2);
                if (distance > maxDistance) maxDistance = distance;
            }
        }
        return maxDistance;
    }
}
