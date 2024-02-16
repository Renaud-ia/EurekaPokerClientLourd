package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;
import java.util.List;

/**
 * implémente les fonctions de base pour les clusters
 * parent des autres types de clusters
 * utilise le calcul de distance implementé dans ObjetClusterisable
 * @param <T> le type d'objet qu'on veut stocker
 */
public class ClusterDeBase<T extends ObjetClusterisable> extends ObjetClusterisable {
    protected List<T> listeObjets;
    protected float[] centroide;
    public ClusterDeBase() {
        listeObjets = new ArrayList<>();
    }

    // getters

    public List<T> getObjets() {
        return listeObjets;
    }
    public int getEffectif() {
        return listeObjets.size();
    }

    public float[] getCentroide() {
        calculerCentroide();
        return this.centroide;
    }

    /**
     * @return le point le plus proche du centroïde
     */
    public T getCentreCluster() {
        calculerCentroide();
        float minDistance = Float.MAX_VALUE;
        T centreTrouve = null;

        for (T objet : getObjets()) {
            float distance = distance(objet);
            if (distance < minDistance) {
                minDistance = distance;
                centreTrouve = objet;
            }
        }

        if (centreTrouve == null) throw new RuntimeException("Aucun point central trouvé");

        return centreTrouve;
    }

    // calcul du centroide

    public void calculerCentroide() {
        if (listeObjets == null || listeObjets.isEmpty()) return;

        int nombrePoints = this.listeObjets.getFirst().nDimensions();
        int nombreElements = this.listeObjets.size();
        float[] centroide = new float[nombrePoints];
        for (T objet : this.listeObjets) {
            for (int j = 0; j < nombrePoints; j++) {
                centroide[j] += objet.valeursNormalisees()[j] / nombreElements;
            }
        }
        this.centroide = centroide;
    }

    // calcul de la dispersion du cluster

    public float getInertie() {
        float sommeInertie = 0;
        for (T objet : this.listeObjets) {
            sommeInertie += distanceCarree(objet);
        }
        return sommeInertie;
    }

    public float distanceIntraCluster() {
        float distanceTotale = 0;
        int nombreDistances = 0;
        for (int i = 0; i < listeObjets.size(); i++) {
            T objet = listeObjets.get(i);
            for (int j = i + 1; j < listeObjets.size(); j++) {
                T autreObjet = listeObjets.get(j);
                distanceTotale += objet.distance(autreObjet);
                nombreDistances++;
            }
        }
        return distanceTotale / nombreDistances;
    }


    @Override
    protected float[] valeursClusterisables() {
        calculerCentroide();
        return centroide;
    }

    @Override
    public String toString() {
        return "CLUSTER : [" + listeObjets.getFirst().toString() + ", ...]";
    }
}
