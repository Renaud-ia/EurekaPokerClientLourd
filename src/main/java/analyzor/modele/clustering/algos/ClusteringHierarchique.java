package analyzor.modele.clustering.algos;

import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.cluster.DistanceCluster;
import analyzor.modele.clustering.liaison.StrategieFactory;
import analyzor.modele.clustering.liaison.StrategieLiaison;
import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.utils.Bits;

import java.util.*;

// les classes dérivées doivent seulement créer les clusters de Base
public abstract class ClusteringHierarchique<T extends ObjetClusterisable> {
    // todo changer la structure de données de la Matrice et mettre à jour les distances
    // sinon on va avoir un gros problème avec de grands ensembles
    public enum MethodeLiaison {
        MOYENNE, WARD, CENTREE, MEDIANE, SIMPLE, COMPLETE
    }
    private final StrategieLiaison<T> strategieLiaison;
    protected List<ClusterHierarchique<T>> clustersActuels;
    protected int indexActuel;
    protected int effectifMinCluster;
    protected TasModifiable<T> tasModifiable;
    protected List<DistanceCluster<T>> listePaires;
    public ClusteringHierarchique(MethodeLiaison methodeLiaison) {
        StrategieFactory<T> strategieFactory = new StrategieFactory<>(methodeLiaison);
        this.strategieLiaison = strategieFactory.getStrategie();
        indexActuel = 0;
        tasModifiable = new TasModifiable<>();
        listePaires = new ArrayList<>();
    }

    // important les clusters doivent avoir un index
    protected void initialiserMatrice() {
        for (int i = 0; i < clustersActuels.size(); i++) {
            ClusterHierarchique<T> cluster1 = clustersActuels.get(i);

            for (int j = i + 1; j < clustersActuels.size(); j++) {
                ClusterHierarchique<T> cluster2 = clustersActuels.get(j);
                float distance = strategieLiaison.calculerDistance(cluster1, cluster2);
                long indexPaire = genererIndice(cluster1, cluster2);
                DistanceCluster<T> distanceCluster = new DistanceCluster<>(cluster1, cluster2, distance, indexPaire);
                listePaires.add(distanceCluster);
            }
        }
        tasModifiable.initialiser(listePaires);
        // on affecte le plus grand nombre possible
        effectifMinCluster = 1;
    }

    /**
     * crée le cluster suivant selon distance la plus proche
     * @return l'effectif minimum des clusters
     */
    protected Integer clusterSuivant() {
        DistanceCluster<T> pairePlusProche = tasModifiable.pairePlusProche();
        if (pairePlusProche == null) return null;

        // on calcule les distances avec tous les autres clusters
        actualiserDistances(pairePlusProche);

        calculerEffectifs();

        return effectifMinCluster;
    }

    private void calculerEffectifs() {
    }

    void actualiserDistances(DistanceCluster<T> clusterPlusProche) {
        ClusterHierarchique<T> clusterModifie = clusterPlusProche.getPremierCluster();
        ClusterHierarchique<T> clusterSupprime = clusterPlusProche.getPremierCluster();

        // on réaffecte l'index du premier cluster pour aller vite
        ClusterHierarchique<T> clusterFusionne =
                new ClusterHierarchique<>(clusterModifie, clusterSupprime, clusterModifie.getIndex());

        for (DistanceCluster<T> paireCluster : listePaires) {
            if (paireCluster.contient(clusterSupprime)) {
                tasModifiable.supprimer(clusterSupprime.getIndex());
                listePaires.remove(paireCluster);
                break;
            }

            // l'index de la distance n'a pas changé
            ClusterHierarchique<T> autreCluster = null;
            if (paireCluster.getPremierCluster() == clusterModifie) {
                paireCluster.setPremierCluster(clusterFusionne);
                autreCluster = paireCluster.getSecondCluster();
            }
            else if (paireCluster.getSecondCluster() == clusterModifie) {
                paireCluster.setSecondCluster(clusterFusionne);
                autreCluster = paireCluster.getPremierCluster();
            }
            if (autreCluster == null) continue;
            float nouvelleDistance = strategieLiaison.calculerDistance(clusterModifie, autreCluster);
            paireCluster.setDistance(nouvelleDistance);
            tasModifiable.actualiser(paireCluster);

        }
    }

    long genererIndice(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2) {
        int bitsNecessaires = Bits.bitsNecessaires(cluster1.getIndex()) + Bits.bitsNecessaires(cluster2.getIndex());
        if (bitsNecessaires >= 63) throw new IllegalArgumentException("Les index sont trop grands : trop de valeurs initiales");
        return ((long) cluster1.getIndex() << 32) | cluster2.getIndex();
    }
}