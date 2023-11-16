package analyzor.modele.clustering.algos;

import analyzor.modele.clustering.cluster.BaseCluster;
import analyzor.modele.clustering.cluster.ClusterDBSCAN;
import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.clustering.objets.ObjetIndexable;
import org.hibernate.collection.spi.BagSemantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * implémentation naïve
 * todo : implémenter un quad-tree
 */
public class DBScan<T extends ObjetClusterisable> {
    private LinkedList<ObjetIndexable<T>> pointsDepart;
    private HashMap<ObjetIndexable, Boolean> pointParcouru;
    private final float epsilon;
    private final int minPoints;
    private final List<ClusterDBSCAN<T>> clusters;
    public DBScan(float epsilon, int minPoints) {
        this.epsilon = epsilon;
        this.minPoints = minPoints;
        this.clusters = new ArrayList<>();
        this.pointParcouru = new HashMap<>();
        this.pointsDepart = new LinkedList<>();
    }

    public void ajouterDonnees(List<T> data) {
        for (T point : data) {
            ObjetIndexable<T> nouvelObjet = new ObjetIndexable<>(point);
            pointParcouru.put(nouvelObjet, false);
        }
    }

    public void clusteriserDonnees() {
        int i = 0;
        for(ObjetIndexable<T> objet : pointsDepart) {
            if (pointParcouru.get(objet)) continue;
            pointParcouru.put(objet, true);

            List<ObjetIndexable<T>> pointsVoisins = pointsVoisins(objet);

            if (pointsVoisins.size() >= minPoints) {
                ClusterDBSCAN<T> nouveauCluster = new ClusterDBSCAN<>();
                nouveauCluster.ajouterObjet(objet);
                this.clusters.add(nouveauCluster);

                etendreLeCluster(pointsVoisins, nouveauCluster);
            }

            i++;
        }
    }

    private void etendreLeCluster(List<ObjetIndexable<T>> pointsVoisins, ClusterDBSCAN<T> nouveauCluster) {
        for (int i = 0; i < pointsVoisins.size(); i++) {
            ObjetIndexable<T> pointVoisin = pointsVoisins.get(i);

            if (pointParcouru.get(pointVoisin)) continue;
            // on met le point voisin comme parcouru
            pointParcouru.put(pointVoisin, true);
            // on l'ajoute au cluster
            nouveauCluster.ajouterObjet(pointVoisin);

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
