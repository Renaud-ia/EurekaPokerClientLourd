package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseCluster<T extends ObjetClusterisable> {
    protected List<T> listeObjets;
    protected float[] centroide;
    protected float[] poids;

    public BaseCluster() {
        listeObjets = new ArrayList<>();
    }

    public BaseCluster(float[] poids) {
        this();
        this.poids = poids;
    }

    public List<T> getObjets() {
        return listeObjets;
    }
    public int getEffectif() {
        return listeObjets.size();
    }

    public void calculerCentroide() {
        int nombrePoints = this.listeObjets.get(0).nDimensions();
        int nombreElements = this.listeObjets.size();
        float[] centroide = new float[nombrePoints];
        for (T objet : this.listeObjets) {
            for (int j = 0; j < nombrePoints; j++) {
                centroide[j] += objet.valeursClusterisables()[j] / nombreElements;
            }
        }
        this.centroide = centroide;
    }

    public float[] getCentroide() {
        calculerCentroide();
        return this.centroide;
    }

    public float getInertie() {
        float sommeInertie = 0;
        for (T objet : this.listeObjets) {
            sommeInertie += distanceCarree(objet);
        }
        return sommeInertie;
    }

    public float distance(T objet) {
        return (float) Math.sqrt(distanceCarree(objet));
    }

    private float distanceCarree(T objet) {
        int nombrePoints = objet.nDimensions();
        fixerPoids();
        if (nombrePoints != centroide.length) throw new IllegalArgumentException("Le centroide et l'objet n'ont pas le même nombre de dimensions");
        float distanceAuCarre  = 0;
        for (int j = 0; j < nombrePoints; j++) {
            distanceAuCarre  += (float) Math.pow(centroide[j] - objet.valeursClusterisables()[j] * poids[j], 2) ;
        }
        return distanceAuCarre;
    }

    public void setPoids(float[] poids) {
        this.poids = poids;
    }

    public float[] getPoids() {
        fixerPoids();
        return poids;
    }

    private void fixerPoids() {
        if (poids == null) {
            poids = new float[this.centroide.length];
            Arrays.fill(poids, 1);
        }
    }
}
