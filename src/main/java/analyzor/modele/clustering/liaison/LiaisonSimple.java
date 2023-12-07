package analyzor.modele.clustering.liaison;

import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.objets.ObjetClusterisable;

public class LiaisonSimple<T extends ObjetClusterisable> extends StrategieLiaison<T> {
    @Override
    public float calculerDistance(ClusterFusionnable<T> cluster1, ClusterFusionnable<T> cluster2) {
        float minDistance = 10000;
        for (ObjetClusterisable objet1 : cluster1.getObjets()) {
            for (ObjetClusterisable objet2 : cluster2.getObjets()) {
                float distance = objet1.distance(objet2);
                if (distance < minDistance) minDistance = distance;
            }
        }
        return minDistance;
    }
}
