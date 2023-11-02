package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.cluster.DistanceCluster;
import analyzor.modele.clustering.cluster.StrategieFactory;
import analyzor.modele.clustering.cluster.StrategieLiaison;
import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.*;

// les classes dérivées doivent seulement créer les clusters de Base
public abstract class ClusteringHierarchique<T extends ObjetClusterisable> {
    public enum MethodeLiaison {
        MOYENNE, WARD, CENTREE, MEDIANE
    }
    private final StrategieLiaison<T> strategieLiaison;
    protected final List<ClusterHierarchique<T>> clustersActuels;
    private final PriorityQueue<DistanceCluster<T>> matriceDistances;
    // utilisé pour vérifier rapidement si la distance retenue correspond à des clusters encore actifs
    private final ArrayList<Boolean> clusterSupprime;
    // important le hashcode se base sur le numéro d'index
    protected int indexActuel;
    protected int effectifMinCluster;
    public ClusteringHierarchique(MethodeLiaison methodeLiaison) {
        StrategieFactory<T> strategieFactory = new StrategieFactory<>(methodeLiaison);
        this.strategieLiaison = strategieFactory.getStrategie();
        matriceDistances = new PriorityQueue<>(
                Comparator.comparingDouble(DistanceCluster::getDistance)
        );
        clustersActuels = new ArrayList<>();
        clusterSupprime = new ArrayList<>();
        indexActuel = 0;
    }

    private void initialiserMatrice() {
        for (int i = 0; i < clustersActuels.size(); i++) {
            for (int j = i + 1; j < clustersActuels.size(); j++) {
                ClusterHierarchique<T> cluster1 = clustersActuels.get(i);
                ClusterHierarchique<T> cluster2 = clustersActuels.get(j);

                float distance = strategieLiaison.calculerDistance(cluster1, cluster2);
                DistanceCluster<T> distanceCluster = new DistanceCluster<>(cluster1, cluster2, distance);
                matriceDistances.add(distanceCluster);
            }
        }
        // à la base un objet par cluster
        effectifMinCluster = 1;
    }

    /**
     * crée le cluster suivant selon distance la plus proche
     * @return l'effectif minimum des clusters
     */
    Integer clusterSuivant() {
        ClusterHierarchique<T> nouveauCluster = clusterPlusProche();
        if (nouveauCluster == null) return null;

        // on calcule les distances avec tous les autres clusters
        calculerDistances(nouveauCluster);

        // on le rajoute dans les listes
        clustersActuels.add(nouveauCluster);
        clusterSupprime.add(true);

        if (nouveauCluster.getEffectif() > effectifMinCluster) effectifMinCluster = nouveauCluster.getEffectif();

        return effectifMinCluster;
    }

    ClusterHierarchique<T> clusterPlusProche() {
        if (clustersActuels.size() < 2) return null;
        boolean distanceInvalide = true;
        DistanceCluster<T> distanceRetenue;
        ClusterHierarchique<T> cluster1 = null;
        ClusterHierarchique<T> cluster2 = null;
        while(distanceInvalide) {
            // on récupère la distance la plus courte
            distanceRetenue = matriceDistances.poll();
            if (distanceRetenue == null) return null;
            cluster1 = Objects.requireNonNull(distanceRetenue).getPremierCluster();
            cluster2 = distanceRetenue.getSecondCluster();

            // si les deux clusters sont toujours actifs
            if (!clusterSupprime.get(cluster1.getIndex()) && !clusterSupprime.get(cluster2.getIndex())) {
                distanceInvalide = false;
            }
        }
        // on supprime les anciens clusters
        supprimerCluster(cluster1);
        supprimerCluster(cluster2);
        clusterSupprime.set(cluster1.getIndex(), true);
        clusterSupprime.set(cluster2.getIndex(), true);

        // on en crée un nouveau
        return new ClusterHierarchique<>(cluster1, cluster2, indexActuel++);
    }

    void calculerDistances(ClusterHierarchique<T> nouveauCluster) {
        for (ClusterHierarchique<T> autreCluster : clustersActuels) {
            if (autreCluster == nouveauCluster) continue;
            float distance = strategieLiaison.calculerDistance(nouveauCluster, autreCluster);
            DistanceCluster<T> distanceCluster = new DistanceCluster<>(nouveauCluster, autreCluster, distance);
            matriceDistances.add(distanceCluster);
        }
    }

    // méthode rapide pour supprimer un cluster de la liste (0(1)) plutôt que O(n)
    // on échange avec le dernier puis on supprime le dernier
    private void supprimerCluster(ClusterHierarchique<T> clusterSupprime) {
        int lastIndex = clustersActuels.size() - 1;
        clustersActuels.set(clusterSupprime.getIndex(), clustersActuels.get(lastIndex));
        clustersActuels.remove(lastIndex);
    }
}
