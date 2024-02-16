package analyzor.modele.clustering.algos;

import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.cluster.DistanceCluster;
import analyzor.modele.clustering.liaison.StrategieFactory;
import analyzor.modele.clustering.liaison.StrategieLiaison;
import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.utils.Bits;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// les classes dérivées doivent seulement créer les clusters de Base
public class ClusteringHierarchique<T extends ObjetClusterisable> {
    // todo changer la structure de données de la Matrice et mettre à jour les distances
    // sinon on va avoir un gros problème avec de grands ensembles
    protected static Logger logger = LogManager.getLogger(ClusteringHierarchique.class);
    public enum MethodeLiaison {
        MOYENNE, WARD, CENTREE, MEDIANE, SIMPLE, COMPLETE
    }
    protected final StrategieLiaison<T> strategieLiaison;
    protected List<ClusterFusionnable<T>> clustersActuels;
    protected int indexActuel;
    protected int effectifMinCluster;
    protected TasModifiable<T> tasModifiable;
    protected HashMap<Long, DistanceCluster<T>> listePaires;
    private int nombreIterations;
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
        nombreIterations = 0;
    }

    // on a abandonné l'idée de itération minimum car dépend du nombre d'objets initiaux dans les clusters
    protected void calculerMinEffectif() {
        this.objectifMinCluster = true;
    }

    /**
     * interface pour utilisation directe
     */
    public void construireClustersDeBase(List<T> objetsOrigines) {
        for (T objet : objetsOrigines) {
            ClusterFusionnable<T> clusterAssocie = new ClusterFusionnable<>(objet, indexActuel++);
            clustersActuels.add(clusterAssocie);
        }

        initialiserMatrice();
    }

    // important les clusters doivent avoir un index
    protected void initialiserMatrice() {
        logger.debug("DEBUT CALCUL MATRICE");
        List<DistanceCluster<T>> toutesLesPaires = new ArrayList<>();
        for (int i = 0; i < clustersActuels.size(); i++) {
            objetsInitiaux++;
            ClusterFusionnable<T> cluster1 = clustersActuels.get(i);

            for (int j = i + 1; j < clustersActuels.size(); j++) {
                ClusterFusionnable<T> cluster2 = clustersActuels.get(j);
                float distance = strategieLiaison.calculerDistance(cluster1, cluster2);
                long indexPaire = genererIndice(cluster1, cluster2);
                DistanceCluster<T> distanceCluster = new DistanceCluster<>(cluster1, cluster2, distance, indexPaire);
                listePaires.put(indexPaire, distanceCluster);
                toutesLesPaires.add(distanceCluster);
                //logger.trace("Distance entre " + cluster1 + " et " + cluster2 + " : " + distance);
                if (distance == 0) logger.error("Distance nulle, erreur probable");
            }
        }
        logger.debug("INITIALISATION DU TAS");
        tasModifiable.initialiser(toutesLesPaires);
        calculerEffectifs();
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

        logger.trace("ITERATION : " + ++nombreIterations);
        logger.trace("Distance plus proche : " + pairePlusProche.getDistance());

        return effectifMinCluster;
    }

    /**
     * utilisation publique de l'algo
     * attention, la sortie considère que les index vont être répercutés de cluster en cluster
     * todo on pourrait aussi transmettre le nouvel index
     */
    public Pair<Integer, Integer> indexFusionnes() {
        DistanceCluster<T> pairePlusProche = tasModifiable.pairePlusProche();
        if (pairePlusProche == null) return null;

        actualiserDistances(pairePlusProche);
        return new Pair<>(pairePlusProche.getPremierCluster().getIndex(), pairePlusProche.getSecondCluster().getIndex());
    }

    private void calculerEffectifs() {
        int minEffectif = Integer.MAX_VALUE;
        for (ClusterFusionnable<T> cluster : clustersActuels) {
            if (cluster.getEffectif() < minEffectif) {
                minEffectif = cluster.getEffectif();
            }
        }
        effectifMinCluster = minEffectif;
    }

    /**
     * important : il faut garder le mécanisme de répercution des clusters sinon indexFusionnes va merder!!
     */
    void actualiserDistances(DistanceCluster<T> pairePlusProche) {
        // important il faut supprimer dans le tas
        listePaires.remove(pairePlusProche.getIndex());
        tasModifiable.supprimer(pairePlusProche.getIndex());

        ClusterFusionnable<T> clusterModifie = pairePlusProche.getPremierCluster();
        ClusterFusionnable<T> clusterSupprime = pairePlusProche.getSecondCluster();

        // on réaffecte l'index du premier cluster pour aller vite
        ClusterFusionnable<T> clusterFusionne =
                new ClusterFusionnable<>(clusterModifie, clusterSupprime, clusterModifie.getIndex());

        clustersActuels.remove(clusterModifie);
        clustersActuels.remove(clusterSupprime);

        for (ClusterFusionnable<T> autreCluster : clustersActuels) {
            modifierDistance(clusterModifie, autreCluster, clusterFusionne);
            supprimerDistance(clusterSupprime, autreCluster);
        }

        clustersActuels.add(clusterFusionne);
    }

    private void supprimerDistance(ClusterFusionnable<T> clusterSupprime, ClusterFusionnable<T> autreCluster) {
        // pour aller vite, on va générer tous les index possibles et voir s'ils sont dans la map
        long[] combinaisonsIndices = combinaisonIndice(clusterSupprime, autreCluster);

        for (long indexSuppression : combinaisonsIndices) {
            DistanceCluster<T> distanceSupprimee = listePaires.remove(indexSuppression);

            if (distanceSupprimee != null) {
                tasModifiable.supprimer(distanceSupprimee.getIndex());
            }
        }
    }

    private void modifierDistance(ClusterFusionnable<T> clusterModifie, ClusterFusionnable<T> autreCluster,
                                  ClusterFusionnable<T> clusterFusionne) {
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

    long[] combinaisonIndice(ClusterFusionnable<T> cluster1, ClusterFusionnable<T> cluster2) {
        long[] combinaison = new long[2];
        combinaison[0] = genererIndice(cluster1, cluster2);
        combinaison[1] = genererIndice(cluster2, cluster1);

        return combinaison;
    }

    long genererIndice(ClusterFusionnable<T> cluster1, ClusterFusionnable<T> cluster2) {
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

    private float inertieActuelle() {
        float inertieTotale = 0;
        for (ClusterFusionnable<T> clusterKMeans : clustersActuels) {
            if (clusterKMeans.getEffectif() == 0) continue;
            inertieTotale += clusterKMeans.getInertie();
        }
        return inertieTotale;
    }

    protected float distanceMoyenneIntraCluster() {
        float distance = 0;
        int nombreClusters = 0;
        for (ClusterFusionnable<T> clusterFusionnable : clustersFormes()) {
            distance += clusterFusionnable.distanceIntraCluster();
            nombreClusters++;
        }

        if (nombreClusters == 0) return 0;

        return distance / nombreClusters;
    }

    private List<ClusterFusionnable<T>> clustersFormes() {
        List<ClusterFusionnable<T>> clustersFormes = new ArrayList<>();

        for (ClusterFusionnable<T> clusterFusionnable : clustersActuels) {
            if (clusterFusionnable.getEffectif() <= 1) continue;
            clustersFormes.add(clusterFusionnable);
        }

        return clustersFormes;
    }
}