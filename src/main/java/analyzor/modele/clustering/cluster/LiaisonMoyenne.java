package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

/**
 * algorithme implémentant la méthode de liaison moyenne
 * moyenne des distances des points des différents clusters
 * @param <T> un objet clusterisable contenu dans les clusters
 */
class LiaisonMoyenne<T extends ObjetClusterisable> extends StrategieLiaison<T> {
    @Override
    public float calculerDistance(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2) {
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