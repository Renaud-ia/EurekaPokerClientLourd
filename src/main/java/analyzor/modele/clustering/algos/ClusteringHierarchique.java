package analyzor.modele.clustering.algos;

import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.cluster.DistanceCluster;
import analyzor.modele.clustering.liaison.StrategieFactory;
import analyzor.modele.clustering.liaison.StrategieLiaison;
import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.utils.Bits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    protected HashMap<Long, DistanceCluster<T>> listePaires;
    private int nombreIterations;
    private int minimumIterations;
    private boolean objectifMinCluster;
    private int objetsInitiaux;
    public ClusteringHierarchique(MethodeLiaison methodeLiaison) {
        StrategieFactory<T> strategieFactory = new StrategieFactory<>(methodeLiaison);
        this.strategieLiaison = strategieFactory.getStrategie();
        indexActuel = 1;
        tasModifiable = new TasModifiable<>();
        listePaires = new HashMap<>();
        this.clustersActuels = new ArrayList<>();
        objetsInitiaux = 0;
        this.objectifMinCluster = false;
    }

    protected void setMinimumPoints(int objectifMinCluster) {
        minimumIterations = iterationsMinimum(clustersActuels.size(), objectifMinCluster);
        this.objectifMinCluster = true;
    }

    // important les clusters doivent avoir un index
    protected void initialiserMatrice() {
        System.out.println("DEBUT CALCUL MATRICE");
        List<DistanceCluster<T>> toutesLesPaires = new ArrayList<>();
        for (int i = 0; i < clustersActuels.size(); i++) {
            objetsInitiaux++;
            ClusterHierarchique<T> cluster1 = clustersActuels.get(i);

            for (int j = i + 1; j < clustersActuels.size(); j++) {
                ClusterHierarchique<T> cluster2 = clustersActuels.get(j);
                float distance = strategieLiaison.calculerDistance(cluster1, cluster2);
                long indexPaire = genererIndice(cluster1, cluster2);
                DistanceCluster<T> distanceCluster = new DistanceCluster<>(cluster1, cluster2, distance, indexPaire);
                listePaires.put(indexPaire, distanceCluster);
                toutesLesPaires.add(distanceCluster);
            }
        }

        tasModifiable.initialiser(toutesLesPaires);
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
        if (objectifMinCluster) calculerEffectifs();

        return effectifMinCluster;
    }

    private void calculerEffectifs() {
        // si le nombre théorique de l'effectif minimum < objectif, on ne vérifie rien
        nombreIterations++;

        if (nombreIterations < minimumIterations) effectifMinCluster = 1;

        // todo on pourrait les foutre dans une PriorityQueue pour accélérer encore mais c'est ok
        //sinon on vérifie tous les clusters
        else {
            int minEffectif = objetsInitiaux;
            for (ClusterHierarchique<T> cluster : clustersActuels) {
                if (cluster.getEffectif() < minEffectif) {
                    minEffectif = cluster.getEffectif();
                }
            }
            effectifMinCluster = minEffectif;
        }

    }

    void actualiserDistances(DistanceCluster<T> pairePlusProche) {
        // important il faut supprimer dans le tas
        listePaires.remove(pairePlusProche.getIndex());
        tasModifiable.supprimer(pairePlusProche.getIndex());

        ClusterHierarchique<T> clusterModifie = pairePlusProche.getPremierCluster();
        ClusterHierarchique<T> clusterSupprime = pairePlusProche.getSecondCluster();

        // on réaffecte l'index du premier cluster pour aller vite
        ClusterHierarchique<T> clusterFusionne =
                new ClusterHierarchique<>(clusterModifie, clusterSupprime, clusterModifie.getIndex());

        clustersActuels.remove(clusterModifie);
        clustersActuels.remove(clusterSupprime);

        for (ClusterHierarchique<T> autreCluster : clustersActuels) {
            modifierDistance(clusterModifie, autreCluster, clusterFusionne);
            supprimerDistance(clusterSupprime, autreCluster);
        }

        clustersActuels.add(clusterFusionne);
    }

    private void supprimerDistance(ClusterHierarchique<T> clusterSupprime, ClusterHierarchique<T> autreCluster) {
        // pour aller vite, on va générer tous les index possibles et voir s'ils sont dans la map
        long[] combinaisonsIndices = combinaisonIndice(clusterSupprime, autreCluster);

        for (long indexSuppression : combinaisonsIndices) {
            DistanceCluster<T> distanceSupprimee = listePaires.remove(indexSuppression);

            if (distanceSupprimee != null) {
                tasModifiable.supprimer(distanceSupprimee.getIndex());
            }
        }
    }

    private void modifierDistance(ClusterHierarchique<T> clusterModifie, ClusterHierarchique<T> autreCluster,
                                  ClusterHierarchique<T> clusterFusionne) {
        long[] combinaisonsIndices = combinaisonIndice(clusterModifie, autreCluster);

        int clustersModifies = 0;

        for (int i = 0; i < combinaisonsIndices.length; i++) {
            long indexModification = combinaisonsIndices[i];
            DistanceCluster<T> distanceModifiee = listePaires.get(indexModification);
            if (distanceModifiee == null) continue;

            distanceModifiee.modifierCluster(clusterModifie, clusterFusionne);
            float nouvelleDistance = strategieLiaison.calculerDistance(
                    distanceModifiee.getPremierCluster(), distanceModifiee.getSecondCluster());
            distanceModifiee.setDistance(nouvelleDistance);
            tasModifiable.actualiser(distanceModifiee);
            clustersModifies++;
        }
        if (clustersModifies != 1) throw new RuntimeException("Nombre de clusters modifiés : " + clustersModifies);
    }

    long[] combinaisonIndice(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2) {
        long[] combinaison = new long[2];
        combinaison[0] = genererIndice(cluster1, cluster2);
        combinaison[1] = genererIndice(cluster2, cluster1);

        return combinaison;
    }

    long genererIndice(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2) {
        int bitsNecessaires = Bits.bitsNecessaires(cluster1.getIndex()) + Bits.bitsNecessaires(cluster2.getIndex());
        if (bitsNecessaires >= 63) throw new IllegalArgumentException("Les index sont trop grands : trop de valeurs initiales");
        return ((long) cluster1.getIndex() << 32) | cluster2.getIndex();
    }

    private int iterationsMinimum(int N, int E) {
        int iTotal = 0;
        int log2E = (int) Math.floor(Math.log(E) / Math.log(2));

        for (int k = 1; k < log2E; k++) {
            iTotal += (int) (N / Math.pow(2, k));
        }

        return iTotal;
    }
}