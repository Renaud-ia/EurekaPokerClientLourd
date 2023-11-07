package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

public class ClusterKMeans<T extends ObjetClusterisable> extends BaseCluster<T> {
    public ClusterKMeans(float[] centroide) {
        this.centroide = centroide;
    }

    public void viderCluster() {
        this.listeObjets.clear();
    }

    public void ajouterObjet(T objet) {
        this.listeObjets.add(objet);
    }

    public float getInertie() {

        float sommeInertie = 0;
        for (T objet : this.listeObjets) {
            sommeInertie += distanceCarree(objet);
        }
        return sommeInertie;
    }

    public int dimensionsCentroide() {
        return centroide.length;
    }

    public float distance(T objet) {
        return (float) Math.sqrt(distanceCarree(objet));
    }

    private float distanceCarree(T objet) {
        int nombrePoints = objet.nDimensions();
        if (nombrePoints != centroide.length) throw new IllegalArgumentException("Le centroide et l'objet n'ont pas le même nombre de dimensions");
        float distanceAuCarre  = 0;
        for (int j = 0; j < nombrePoints; j++) {
            distanceAuCarre  += (float) Math.pow(centroide[j] - objet.valeursClusterisables()[j], 2);
        }
        return distanceAuCarre;
    }
}
