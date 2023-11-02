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
        MOYENNE, WARD, CENTREE, MEDIANE, SIMPLE, COMPLETE
    }
    private final StrategieLiaison<T> strategieLiaison;
    protected final List<ClusterHierarchique<T>> clustersActuels;
    private final PriorityQueue<DistanceCluster<T>> matriceDistances;
    // utilisé pour vérifier rapidement si la distance retenue correspond à des clusters encore actifs
    protected final HashMap<Integer, Boolean> clusterSupprime;
    protected int indexActuel;
    protected int effectifMinCluster;
    private final float poidsPenaliteDistance = 1.5f;
    public ClusteringHierarchique(MethodeLiaison methodeLiaison) {
        StrategieFactory<T> strategieFactory = new StrategieFactory<>(methodeLiaison);
        this.strategieLiaison = strategieFactory.getStrategie();
        matriceDistances = new PriorityQueue<>(
                Comparator.comparingDouble(DistanceCluster::getDistance)
        );
        clustersActuels = new ArrayList<>();
        clusterSupprime = new HashMap<>();
        indexActuel = 0;
    }

    protected void initialiserMatrice() {
        for (int i = 0; i < clustersActuels.size(); i++) {
            ClusterHierarchique<T> cluster1 = clustersActuels.get(i);

            for (int j = i + 1; j < clustersActuels.size(); j++) {
                ClusterHierarchique<T> cluster2 = clustersActuels.get(j);
                float distance = strategieLiaison.calculerDistance(cluster1, cluster2);
                DistanceCluster<T> distanceCluster = new DistanceCluster<>(cluster1, cluster2, distance);
                matriceDistances.add(distanceCluster);
            }
        }
        // on affecte le plus grand nombre possible
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
        clusterSupprime.put(nouveauCluster.getIndex(), false);

        Collections.shuffle(clustersActuels);

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
        clusterSupprime.put(cluster1.getIndex(), true);
        clusterSupprime.put(cluster2.getIndex(), true);

        // on en crée un nouveau
        return new ClusterHierarchique<>(cluster1, cluster2, indexActuel++);
    }

    void calculerDistances(ClusterHierarchique<T> nouveauCluster) {
        int nouveauMinEffectifCluster = nouveauCluster.getEffectif();

        int compteur = 0;
        int maxComparaison = 200;
        for (ClusterHierarchique<T> autreCluster : clustersActuels) {
            if (compteur++ > maxComparaison) break;
            if (autreCluster == nouveauCluster) continue;
            float distance = strategieLiaison.calculerDistance(nouveauCluster, autreCluster);
            float penaliteDistance = poidsPenaliteDistance * (nouveauCluster.getEffectif() + autreCluster.getEffectif());

            DistanceCluster<T> distanceCluster = new DistanceCluster<>(nouveauCluster, autreCluster, distance * penaliteDistance);

            matriceDistances.add(distanceCluster);

            // on en profite pour compter le nombre minimum d'éffectifs dans chaque cluster
            if (autreCluster.getEffectif() < nouveauMinEffectifCluster)
                nouveauMinEffectifCluster = autreCluster.getEffectif();
        }

        effectifMinCluster = nouveauMinEffectifCluster;
    }

    // todo OPTIMISATION : attention car l'index du cluster ne correspond plus forcément à sa position dans la liste
    private void supprimerCluster(ClusterHierarchique<T> clusterSupprime) {
        clustersActuels.remove(clusterSupprime);
    }
}
