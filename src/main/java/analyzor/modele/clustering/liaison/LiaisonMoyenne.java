package analyzor.modele.clustering.liaison;

import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.objets.ObjetClusterisable;


class LiaisonMoyenne<T extends ObjetClusterisable> extends StrategieLiaison<T> {
    @Override
    public float calculerDistance(ClusterFusionnable<T> cluster1, ClusterFusionnable<T> cluster2) {
        int nDistances = 0;
        float sommeDistance = 0;
        for (ObjetClusterisable objet1 : cluster1.getObjets()) {
            for (ObjetClusterisable objet2 : cluster2.getObjets()) {
                sommeDistance += objet1.distance(objet2);
                nDistances++;
            }
        }
        if (nDistances == 0) throw new IllegalArgumentException("Un des clusters ne contient aucun objet");
        return sommeDistance / nDistances;
    }
}