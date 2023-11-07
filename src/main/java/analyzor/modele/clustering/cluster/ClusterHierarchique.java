package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;
import java.util.List;

public class ClusterHierarchique<T extends ObjetClusterisable> {
    List<T> listeObjets;
    int index;

    public ClusterHierarchique(T objetDepart, int indexCluster) {
        listeObjets = new ArrayList<>();
        listeObjets.add(objetDepart);
        index = indexCluster;
    }

    public ClusterHierarchique(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2, int indexCluster) {
        if (cluster1 == null || cluster2 == null) {
            throw new IllegalArgumentException("Un des clusters est null");
        }
        this.listeObjets = new ArrayList<>();
        this.listeObjets.addAll(cluster1.getObjets());
        this.listeObjets.addAll(cluster2.getObjets());
        this.index = indexCluster;
    }

    public List<T> getObjets() {
        return listeObjets;
    }

    public int getIndex() {
        return index;
    }

    public int getEffectif() {
        return listeObjets.size();
    }

    public float[] getCentroide() {
        int nombrePoints = this.listeObjets.get(0).nombrePoints();
        int nombreElements = this.listeObjets.size();
        float[] centroide = new float[nombrePoints];
        for (T objet : this.listeObjets) {
            for (int j = 0; j < nombrePoints; j++) {
                centroide[j] += objet.valeursClusterisables()[j] / nombreElements;
            }
        }
        return centroide;
    }

    public void fusionner(ClusterHierarchique<T> clusterInitial) {
        this.listeObjets.addAll(clusterInitial.getObjets());
    }
}
