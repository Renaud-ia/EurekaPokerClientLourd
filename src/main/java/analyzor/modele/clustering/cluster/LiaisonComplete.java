package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

// méthode de liaison basée sur la plus grande distance
public class LiaisonComplete<T extends ObjetClusterisable> extends StrategieLiaison<T> {
    @Override
    public float calculerDistance(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2) {
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
