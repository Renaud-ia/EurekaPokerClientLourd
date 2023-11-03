package analyzor.modele.clustering.liaison;

import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.objets.ObjetClusterisable;

/**
 * algorithme implémentant la méthode de liaison centrée
 *  distance des centroïdes
 * @param <T> un objet clusterisable contenu dans les clusters
 */
class LiaisonCentree<T extends ObjetClusterisable> extends StrategieLiaison<T> {
    @Override
    public float calculerDistance(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2) {
        float[] centroide1 = cluster1.getCentroide();
        float[] centroide2 = cluster2.getCentroide();

        if (centroide1.length != centroide2.length) {
            throw new IllegalArgumentException("Les deux clusters n'ont pas le même nombre de dimensions");
        }

        float sommeDesCarres = 0;
        for (int i = 0; i < centroide1.length; i++) {
            sommeDesCarres += (float) Math.pow(centroide1[i] - centroide2[i], 2);
        }

        float distance = (float) Math.sqrt(sommeDesCarres);
        return distance;
    }
}
