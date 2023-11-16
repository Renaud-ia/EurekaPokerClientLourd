package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.List;

public class BaseCluster<T extends ObjetClusterisable> {
    protected List<T> listeObjets;
    protected float[] centroide;

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
}
