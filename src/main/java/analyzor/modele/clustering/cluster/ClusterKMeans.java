package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;

public class ClusterKMeans<T extends ObjetClusterisable> extends BaseCluster<T> {
    private final float[] poids;
    public ClusterKMeans(float[] centroide, float[] poids) {
        this.centroide = centroide;
        listeObjets = new ArrayList<>();
        this.poids = poids;
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
            distanceAuCarre  += (float) Math.pow(centroide[j] - objet.valeursClusterisables()[j] * poids[j], 2) ;
        }
        return distanceAuCarre;
    }

    public void setCentroide(float[] nouveauCentroide) {
        this.centroide = nouveauCentroide;
    }

    // on veut calculer manuellement les centroides dans le cas de KMeans
    @Override
    public float[] getCentroide() {
        return centroide;
    }
}
