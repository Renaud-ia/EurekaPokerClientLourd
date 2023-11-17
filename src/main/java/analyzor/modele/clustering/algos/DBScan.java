package analyzor.modele.clustering.algos;

import analyzor.modele.clustering.cluster.ClusterDBSCAN;
import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.clustering.objets.ObjetIndexable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * implémentation naïve du DBSCAN
 * todo OPTIMISATION : implémenter un quad-tree
 */
public class DBScan<T extends ObjetClusterisable> {
    private final LinkedList<ObjetIndexable<T>> pointsDepart;
    private final HashMap<ObjetIndexable<T>, Boolean> pointParcouru;
    private final float epsilon;
    private int minPoints;
    protected final List<ClusterDBSCAN<T>> clusters;
    public DBScan(float epsilon) {
        this.epsilon = epsilon;
        this.clusters = new ArrayList<>();
        this.pointParcouru = new HashMap<>();
        this.pointsDepart = new LinkedList<>();
    }

    public void construireDonnees(List<T> data) {
        for (T point : data) {
            ObjetIndexable<T> nouvelObjet = new ObjetIndexable<>(point);
            pointsDepart.add(nouvelObjet);
            pointParcouru.put(nouvelObjet, false);
        }
    }

    public void clusteriserDonnees(int minPoints) {
        this.minPoints = minPoints;
        for(ObjetIndexable<T> objet : pointsDepart) {
            if (pointParcouru.get(objet)) continue;
            pointParcouru.put(objet, true);

            List<ObjetIndexable<T>> pointsVoisins = pointsVoisins(objet);

            if (pointsVoisins.size() >= minPoints) {
                System.out.println("CLUSTER TROUVE");
                ClusterDBSCAN<T> nouveauCluster = new ClusterDBSCAN<>();
                nouveauCluster.ajouterObjet(objet);
                this.clusters.add(nouveauCluster);

                etendreLeCluster(pointsVoisins, nouveauCluster);
            }
        }
    }

    private void etendreLeCluster(List<ObjetIndexable<T>> pointsVoisins, ClusterDBSCAN<T> nouveauCluster) {
        for (ObjetIndexable<T> pointVoisin : pointsVoisins) {
            // si le point a déjà été parcouru on l'ignore
            // todo : normalement il faut vérifier s'il a pas déjà un cluster mais bon surement marginal
            if (pointParcouru.get(pointVoisin)) continue;
            // on met le point comme parcouru
            pointParcouru.put(pointVoisin, true);
            // on l'ajoute au cluster
            nouveauCluster.ajouterObjet(pointVoisin);

            // on regarde si c'est un noyau, si oui on étend encore le cluster
            List<ObjetIndexable<T>> autresVoisins = pointsVoisins(pointVoisin);
            if (autresVoisins.size() >= minPoints) {
                etendreLeCluster(autresVoisins, nouveauCluster);
            }
        }

    }

    private List<ObjetIndexable<T>> pointsVoisins(ObjetIndexable<T> point) {
        List<ObjetIndexable<T>> pointsVoisins = new ArrayList<>();
        for (ObjetIndexable<T> autrePoint : pointsDepart) {
            if (autrePoint.equals(point)) continue;
            float distance = point.getObjet().distance(autrePoint.getObjet());
            if (distance < epsilon) pointsVoisins.add(autrePoint);
        }

        return pointsVoisins;
    }

}
