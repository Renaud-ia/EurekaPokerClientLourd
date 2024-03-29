package analyzor.modele.clustering.liaison;

import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.objets.ObjetClusterisable;


class LiaisonWard<T extends ObjetClusterisable> extends StrategieLiaison<T> {
    @Override
    public float calculerDistance(ClusterFusionnable<T> cluster1, ClusterFusionnable<T> cluster2) {
        float[] centroide1 = cluster1.getCentroide();
        float[] centroide2 = cluster2.getCentroide();

        float[] poids = cluster1.getObjets().getFirst().getPoids();

        if (centroide1.length != centroide2.length) {
            throw new IllegalArgumentException("Les deux clusters n'ont pas le même nombre de dimensions");
        }

        float sommeDesCarres = 0;
        for (int i = 0; i < centroide1.length; i++) {
            sommeDesCarres += (float) Math.pow((centroide1[i] - centroide2[i]) * poids[i], 2);
        }

        int tailleCluster1 = cluster1.getEffectif();
        int tailleCluster2 = cluster2.getEffectif();

        
        return ((float) tailleCluster1 * tailleCluster2 * sommeDesCarres) / ((float)tailleCluster1 + tailleCluster2);
    }
}
