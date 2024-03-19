package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

public class ClusterKMeans<T extends ObjetClusterisable> extends ClusterDeBase<T> {
    public ClusterKMeans(float[] centroide, float[] poids) {
        super();
        this.centroide = centroide;
    }

    public void viderCluster() {
        this.listeObjets.clear();
    }

    public void ajouterObjet(T objet) {
        this.listeObjets.add(objet);
    }

    public int dimensionsCentroide() {
        return centroide.length;
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
