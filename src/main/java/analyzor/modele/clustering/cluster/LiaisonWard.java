package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

/**
 * algorithme implémentant la méthode de liaison de Ward
 * calcule la variance interne si on fusionne les deux clusters
 * @param <T> un objet clusterisable contenu dans les clusters
 */
class LiaisonWard<T extends ObjetClusterisable> extends StrategieLiaison<T> {
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

        int tailleCluster1 = cluster1.getEffectif();
        int tailleCluster2 = cluster2.getEffectif();
        return (tailleCluster1 * tailleCluster2 * sommeDesCarres) / (tailleCluster1 + tailleCluster2);
    }
}
