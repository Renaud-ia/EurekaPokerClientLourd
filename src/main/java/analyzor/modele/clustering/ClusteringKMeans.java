package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ClusteringKMeans<T extends ObjetClusterisable> {
    private static float MAX_FLOAT = 10E37F;
    private List<ClusterKMeans<T>> meilleurClustering;
    private final LinkedList<ClusterKMeans<T>> clusteringActuel;
    // pour calcul de convergence
    private final List<float[]> anciensCentroides;
    private List<T> objetsClusterises;
    private float[] valeursMinimum;
    private float[] valeursMaximum;
    private final int MAX_ITER;
    private final int N_INIT;

    public ClusteringKMeans() {
        // valeurs standards
        MAX_ITER = 300;
        N_INIT = 10;

        meilleurClustering = new ArrayList<>();
        clusteringActuel = new LinkedList<>();
        anciensCentroides = new ArrayList<>();
    }

    public void ajouterDonnees(List<T> objetsClusterisables) {
        // on vérifie que tous les objets ont même dimension
        // on calcule les valeurs min et max pour initialisation
        int nDimensions = objetsClusterisables.get(0).nDimensions();
        valeursMinimum = objetsClusterisables.get(0).valeursClusterisables();
        valeursMaximum = objetsClusterisables.get(0).valeursClusterisables();
        for (T objet : objetsClusterisables) {
            if (objet.nDimensions() != nDimensions)
                throw new IllegalArgumentException("Tous les objets n'ont pas la même dimension");

            float[] valeursObjet = objet.valeursClusterisables();
            for (int i=0; i < valeursObjet.length; i++) {
                if (valeursObjet[i] < valeursMinimum[i]) {
                    valeursMinimum[i] = valeursObjet[i];
                }
                if (valeursObjet[i] > valeursMaximum[i]) {
                    valeursMaximum[i] = valeursObjet[i];
                }
            }
        }

        objetsClusterises = objetsClusterisables;
    }

    public void ajusterClusters(int nClusters) {
        float meilleureInertie = MAX_FLOAT;
        for (int i = 0; i <= N_INIT; i++) {
            float inertie = Kmeans(nClusters);
            if (inertie < meilleureInertie) {
                meilleureInertie = inertie;
                meilleurClustering = clusteringActuel;
            }
        }
    }

    public List<ClusterKMeans<T>> getClusters() {
        return meilleurClustering;
    }

    // retourne l'inertie finale après ajustement
    private float Kmeans(int nClusters) {
        // on efface le clustering précédent
        clusteringActuel.clear();
        initialiserClusters(nClusters);

        // todo tester la bonne valeur
        float seuilConvergence = 0.01f;
        for (int i=0; i <= MAX_ITER; i++) {
            reaffecterObjets();
            float changementPositions = mouvementsClusters();
            if (changementPositions < seuilConvergence) break;
        }
        return inertieActuelle();
    }

    // réaffecte les objets au cluster le plus proche
    private void reaffecterObjets() {
        // on vide les clusters et on sauvegarde les anciens centroides
        viderClusters();

        float minDistance = MAX_FLOAT;
        ClusterKMeans<T> clusterPlusProche = null;
        for (T objet : objetsClusterises) {
            // on regarde pour tous les clusters le plus proche
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

        // on recalcule les centroides pour chaque cluster
        for(ClusterKMeans<T> clusterKMeans : clusteringActuel) {
            clusterKMeans.calculerCentroide();
        }
    }

    private void viderClusters() {
        anciensCentroides.clear();
        for (ClusterKMeans<T> clusterKMeans : clusteringActuel) {
            // on sauvegarde l'ancien centroide
            anciensCentroides.add(clusterKMeans.getCentroide());

            // on vide le cluster de ses objets
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

    // distance entre le centroide actuel d'un clsuter et son ancien centroide
    private float distanceCentroides(float[] ancienCentroide, ClusterKMeans<T> nouveauCluster) {
        float distanceCarree = 0;
        for (int i = 0; i < nouveauCluster.dimensionsCentroide(); i++) {
            distanceCarree +=
                    (float) Math.pow(ancienCentroide[i] - nouveauCluster.getCentroide()[i], 2);
        }

        return (float) Math.sqrt(distanceCarree);
    }

    // initialisation des clusters avec centroides random
    private void initialiserClusters(int nClusters) {
        Random r = new Random();
        for (int indexCluster = 0; indexCluster < nClusters; indexCluster++) {
            float [] randomCentroide = new float[valeursMaximum.length];
            for (int i = 0; i < valeursMaximum.length; i++) {
                float valeurRandom = valeursMinimum[i] + r.nextFloat() * (valeursMaximum[i] - valeursMinimum[i]);
                randomCentroide[i] = valeurRandom;
            }
            ClusterKMeans<T> randomCluster = new ClusterKMeans<>(randomCentroide);
            clusteringActuel.add(randomCluster);
        }
    }

    private float inertieActuelle() {
        float inertieTotale = 0;
        for (ClusterKMeans<T> clusterKMeans : clusteringActuel) {
            inertieTotale += clusterKMeans.getInertie();
        }
        return inertieTotale;
    }
}
