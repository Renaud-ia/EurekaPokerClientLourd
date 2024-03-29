package analyzor.modele.clustering.algos;

import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.clustering.objets.ObjetClusterisable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ClusteringKMeans<T extends ObjetClusterisable> {
    private static final float MAX_FLOAT = Float.MAX_VALUE;
    private List<ClusterKMeans<T>> meilleurClustering;
    private final LinkedList<ClusterKMeans<T>> clusteringActuel;

    private final List<float[]> anciensCentroides;
    private List<T> objetsClusterises;
    private float[] valeursMinimum;
    private float[] valeursMaximum;
    private final int MAX_ITER;
    private final int N_INIT;

    protected float[] poids;

    public ClusteringKMeans() {

        MAX_ITER = 500;
        N_INIT = 10;

        meilleurClustering = new ArrayList<>();
        clusteringActuel = new LinkedList<>();
        anciensCentroides = new ArrayList<>();
    }

    public void initialiser(List<T> objetsClusterisables) {


        int nDimensions = objetsClusterisables.getFirst().nDimensions();
        valeursMinimum = objetsClusterisables.getFirst().valeursNormalisees();
        valeursMaximum = objetsClusterisables.getFirst().valeursNormalisees();

        for (T objet : objetsClusterisables) {
            if (objet.nDimensions() != nDimensions)
                throw new IllegalArgumentException("Tous les objets n'ont pas la même dimension");

            float[] valeursObjet = objet.valeursNormalisees();
            for (int i=0; i < valeursObjet.length; i++) {
                if (valeursObjet[i] < valeursMinimum[i]) {
                    valeursMinimum[i] = valeursObjet[i];
                }
                if (valeursObjet[i] > valeursMaximum[i]) {
                    valeursMaximum[i] = valeursObjet[i];
                }
            }
        }



        if (poids == null) {
            poids = new float[nDimensions];
            Arrays.fill(poids, 1);
        }
        else {
            if (poids.length != nDimensions)
                throw new IllegalArgumentException("Les dimensions du poids ne correspondent pas aux dimensions des points");
        }

        objetsClusterises = objetsClusterisables;
    }

    public float ajusterClusters(int nClusters) {
        float meilleureInertie = MAX_FLOAT;
        for (int i = 0; i <= N_INIT; i++) {
            float inertie = Kmeans(nClusters);
            if (inertie < meilleureInertie) {
                meilleureInertie = inertie;
                meilleurClustering = clusteringActuel;
            }
        }

        return meilleureInertie;
    }


    public List<ClusterKMeans<T>> getClusters() {
        List<ClusterKMeans<T>> clustersNonVides = new ArrayList<>();
        for (ClusterKMeans<T> cluster : meilleurClustering) {
            if (cluster.getEffectif() > 0) clustersNonVides.add(cluster);
        }
        return clustersNonVides;
    }


    private float Kmeans(int nClusters) {

        clusteringActuel.clear();
        initialiserClusters(nClusters);


        float seuilConvergence = 0.01f;
        for (int i=0; i <= MAX_ITER; i++) {
            reaffecterObjets(i);
            float changementPositions = mouvementsClusters();
            if (changementPositions < seuilConvergence) break;
            if (i == MAX_ITER) {

                return Float.MAX_VALUE;
            }
            viderClusters();
        }
        return inertieActuelle();
    }


    private void reaffecterObjets(int iterationActuelle) {
        ClusterKMeans<T> clusterPlusProche = null;
        for (T objet : objetsClusterises) {
            float minDistance = MAX_FLOAT;

            for(ClusterKMeans<T> clusterKMeans : clusteringActuel) {
                float distance = clusterKMeans.distance(objet);
                if (distance < minDistance) {
                    minDistance = distance;
                    clusterPlusProche = clusterKMeans;
                }
            }
            assert clusterPlusProche != null;
            clusterPlusProche.ajouterObjet(objet);
        }

    }

    private void viderClusters() {


        anciensCentroides.clear();
        for(ClusterKMeans<T> clusterKMeans : clusteringActuel) {
            anciensCentroides.add(clusterKMeans.getCentroide());
            clusterKMeans.viderCluster();
        }
    }


    private float mouvementsClusters() {
        float sommeDistance = 0;
        int indexCluster = 0;
        for (ClusterKMeans<T> nouveauCluster : clusteringActuel) {
            sommeDistance += distanceCentroides(anciensCentroides.get(indexCluster++), nouveauCluster);
        }

        return sommeDistance / indexCluster;
    }


    private float distanceCentroides(float[] ancienCentroide, ClusterKMeans<T> nouveauCluster) {
        float distanceCarree = 0;
        if (nouveauCluster.getEffectif() == 0) {
            float[] randomCentroide = randomCentroide();
            nouveauCluster.setCentroide(randomCentroide);
        }
        else nouveauCluster.calculerCentroide();

        for (int i = 0; i < nouveauCluster.dimensionsCentroide(); i++) {
            distanceCarree +=
                    (float) Math.pow(ancienCentroide[i] - nouveauCluster.getCentroide()[i], 2);
        }

        return (float) Math.sqrt(distanceCarree);
    }


    private void initialiserClusters(int nClusters) {
        for (int indexCluster = 0; indexCluster < nClusters; indexCluster++) {
            float[] randomCentroide = randomCentroide();
            ClusterKMeans<T> randomCluster = new ClusterKMeans<>(randomCentroide, poids);
            clusteringActuel.add(randomCluster);

            anciensCentroides.add(randomCentroide);
        }
    }

    private float[] randomCentroide() {
        Random r = new Random();
        float [] randomCentroide = new float[valeursMaximum.length];
        for (int i = 0; i < valeursMaximum.length; i++) {
            float valeurRandom = valeursMinimum[i] + r.nextFloat() * (valeursMaximum[i] - valeursMinimum[i]);
            randomCentroide[i] = valeurRandom;
        }

        return randomCentroide;
    }

    private float inertieActuelle() {
        float inertieTotale = 0;
        for (ClusterKMeans<T> clusterKMeans : clusteringActuel) {
            if (clusterKMeans.getEffectif() == 0) return MAX_FLOAT;
            inertieTotale += clusterKMeans.getInertie();
        }
        return inertieTotale;
    }
}
