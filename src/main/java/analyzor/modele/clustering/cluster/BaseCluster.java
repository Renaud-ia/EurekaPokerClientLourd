package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;
import java.util.List;

public class BaseCluster<T extends ObjetClusterisable> {
    // todo REFACTORISATION il faudrait hériter de ObjetClusterisable définir valeursClusterisables()
    //  et utiliser les méthodes de ObjetClusterisable pour toutes les distances
    protected List<T> listeObjets;
    protected float[] centroide;
    protected float[] poids;

    public BaseCluster() {
        listeObjets = new ArrayList<>();
    }

    public List<T> getObjets() {
        return listeObjets;
    }
    public int getEffectif() {
        return listeObjets.size();
    }

    public void calculerCentroide() {
        if (listeObjets == null || listeObjets.isEmpty()) return;

        int nombrePoints = this.listeObjets.get(0).nDimensions();
        int nombreElements = this.listeObjets.size();
        float[] centroide = new float[nombrePoints];
        for (T objet : this.listeObjets) {
            for (int j = 0; j < nombrePoints; j++) {
                centroide[j] += objet.valeursNormalisees()[j] / nombreElements;
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
        float[] poids = objet.getPoids();

        int nombrePoints = objet.nDimensions();
        if (nombrePoints != centroide.length) throw new IllegalArgumentException("Le centroide et l'objet n'ont pas le même nombre de dimensions");
        float distanceAuCarre  = 0;
        for (int j = 0; j < nombrePoints; j++) {
            distanceAuCarre  += (float) Math.pow((centroide[j] - objet.valeursNormalisees()[j]) * poids[j], 2) ;
        }
        return distanceAuCarre;
    }


    @Override
    public String toString() {
        return "CLUSTER : [" + listeObjets.get(0).toString() + ", ...]";
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
}
