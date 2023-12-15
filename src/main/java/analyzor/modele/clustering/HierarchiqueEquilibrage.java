package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.objets.MinMaxCalcul;
import analyzor.modele.equilibrage.ComboEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboDenombrable;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * méthode particulière du clustering pour équilibrage
 * on va regrouper un certain pourcentage de points
 * puis affecter tous les points isolés à un cluster déjà formé
 */
public class HierarchiqueEquilibrage extends ClusteringHierarchique<ComboEquilibrage> {
    // pct de range sur lesquels sont calculés les clusters de densité initiaux
    private final static float PCT_RANGE = 0.6f;
    //min de % de range pour prendre en compte un cluster
    private final static float MIN_PCT_RANGE = 0.02f;
    private final static int MIN_EFFECTIF_CLUSTER = 200;
    private final static MethodeLiaison METHODE_LIAISON = MethodeLiaison.COMPLETE;
    private int nIterations;
    private final int nSituations;
    public HierarchiqueEquilibrage(int nSituations) {
        super(METHODE_LIAISON);
        logger = LogManager.getLogger(HierarchiqueEquilibrage.class);
        this.nSituations = nSituations;
    }

    public void ajouterDonnees(List<ComboEquilibrage> noeuds) {
        nIterations = (int) (PCT_RANGE * noeuds.size());

        normaliserDonnees(noeuds);

        for (ComboEquilibrage noeud : noeuds) {
            ClusterFusionnable<ComboEquilibrage> nouveauCluster = new ClusterFusionnable<>(noeud, indexActuel++);
            clustersActuels.add(nouveauCluster);
        }
        initialiserMatrice();
    }

    public void lancerClustering() {
        for (int i = 0; i < nIterations; i++) {
            this.clusterSuivant();
        }
    }

    public List<ComboEquilibrage> getResultats() {
        ExtensionDensiteRange extensionDensiteRange =
                new ExtensionDensiteRange((float) MIN_EFFECTIF_CLUSTER / nSituations);
        extensionDensiteRange.ajouterCentres(clustersActuels);

        // on crée des noeuds Equilibrage à partir des clusters corrigés
        List<ComboEquilibrage> resultats = new ArrayList<>();

        for (ClusterFusionnable<ComboEquilibrage> cluster : extensionDensiteRange.recupererClusters()) {
            logger.debug("###CLUSTER FORME###");
            List<ComboDenombrable> combosDansNoeud = new ArrayList<>();
            for (ComboEquilibrage comboEquilibrage : cluster.getObjets()) {
                logger.trace(comboEquilibrage);
                logger.trace(Arrays.toString(comboEquilibrage.getStrategie()));
                combosDansNoeud.addAll(comboEquilibrage.getCombosDenombrables());
            }
            ComboEquilibrage noeudParent = new ComboEquilibrage(combosDansNoeud);
            resultats.add(noeudParent);
        }

        return resultats;
    }


    private void normaliserDonnees(List<ComboEquilibrage> noeuds) {
        MinMaxCalcul<ComboEquilibrage> minMaxCalcul = new MinMaxCalcul<>();
        minMaxCalcul.calculerMinMax(0, Float.MIN_VALUE, noeuds);

        // on calcule les valeurs min et max
        float[] minValeurs = minMaxCalcul.getMinValeurs();
        float[] maxValeurs = minMaxCalcul.getMaxValeurs();

        for (ComboEquilibrage comboEquilibrage : noeuds) {
            comboEquilibrage.activerMinMaxNormalisation(minValeurs, maxValeurs);
        }
    }


    /**
     * classe qui va corriger les clusters de densité préalablement formés
     * et va les étendre/fusionner jusqu'à atteindre des clusters significatifs
     * ne travaille qu'en terme d'équité brute
     */
    class ExtensionDensiteRange {
        private final List<ClusterFusionnable<ComboEquilibrage>> clustersGroupes;
        private final  List<ClusterFusionnable<ComboEquilibrage>> pointsIsoles;
        private final float minPctCluster;
        ExtensionDensiteRange(float minPctCluster) {
            clustersGroupes = new ArrayList<>();
            pointsIsoles = new ArrayList<>();
            this.minPctCluster = minPctCluster;
        }

        // reçoit des clusters individuels et groupés
        void ajouterCentres(List<ClusterFusionnable<ComboEquilibrage>> clusters) {
            separerClusters(clusters);
            reaffecterIntrus();
            etendreLesClusters();
            reaffecterIntrus();
            fusionnerClustersTropPetits();
        }

        List<ClusterFusionnable<ComboEquilibrage>> recupererClusters() {
            return clustersGroupes;
        }

        private void separerClusters(List<ClusterFusionnable<ComboEquilibrage>> clusters) {
            // on va séparer les clusters isolés et groupés
            for (ClusterFusionnable<ComboEquilibrage> cluster : clusters) {
                if (cluster.getEffectif() > 1) clustersGroupes.add(cluster);
                else pointsIsoles.add(cluster);
            }
        }

        /**
         * on prend la distance moyenne d'un point avec les autres points de son cluster
         * et on compare avec la distance moyenne des autres points d'un autre cluster
         */
        private void reaffecterIntrus() {
            logger.debug("Réaffectation des intrus");
            for (ClusterFusionnable<ComboEquilibrage> clusterInitial : clustersGroupes) {
                // on crée un itérateur pour pouvoir remove pendant qu'on loop
                Iterator<ComboEquilibrage> iterateurCluster = clusterInitial.getObjets().iterator();
                while(iterateurCluster.hasNext()) {
                    ClusterFusionnable<ComboEquilibrage> clusterAccueil = null;
                    ComboEquilibrage pointCluster = iterateurCluster.next();
                    float distanceInitiale = distanceCluster(pointCluster, clusterInitial);
                    float minAutreDistance = Float.MAX_VALUE;
                    for (ClusterFusionnable<ComboEquilibrage> clusterHote : clustersGroupes) {
                        if (clusterAccueil == clusterHote) continue;
                        float distanceAutreCluster = distanceCluster(pointCluster, clusterHote);
                        if (distanceAutreCluster < minAutreDistance) {
                            minAutreDistance = distanceAutreCluster;
                            clusterAccueil = clusterHote;
                        }
                    }

                    // si on a trouvé une distance inférieure, on réaffecte le point
                    if (minAutreDistance < distanceInitiale) {
                        logger.trace("Intrus trouvé => " + pointCluster + " va être réaffecté à " + clusterAccueil);
                        if (clusterAccueil == null) throw new RuntimeException();
                        clusterAccueil.ajouterObjet(pointCluster);
                        iterateurCluster.remove();
                    }
                }
            }
        }

        private void etendreLesClusters() {
            logger.debug("Extension des clusters");
            // on associé les points individuels à des clusters
            for (ClusterFusionnable<ComboEquilibrage> clusterIsole : pointsIsoles) {
                logger.trace("CLUSTER ORPHELIN : " + clusterIsole);

                ClusterFusionnable<ComboEquilibrage> clusterHote = clusterPlusProche(clusterIsole, clustersGroupes);

                logger.trace("CLUSTER ASSOCIE : " + clusterHote);

                clusterHote.fusionner(clusterIsole);
            }

            pointsIsoles.clear();
        }

        private void fusionnerClustersTropPetits() {
            Iterator<ClusterFusionnable<ComboEquilibrage>> iterateurClusters = clustersGroupes.iterator();
            while(iterateurClusters.hasNext()) {
                ClusterFusionnable<ComboEquilibrage> clusterTraite = iterateurClusters.next();
                // deux critères : x combos servis et en % de range
                if (pctRange(clusterTraite) >= minPctCluster || pctRange(clusterTraite) > MIN_PCT_RANGE) continue;
                logger.trace("Cluster trop petit, on le fusionne : " + clusterTraite);

                ClusterFusionnable<ComboEquilibrage> clusterHote = clusterPlusProche(clusterTraite, clustersGroupes);
                clusterHote.fusionner(clusterTraite);
                iterateurClusters.remove();
            }
        }

        private ClusterFusionnable<ComboEquilibrage> clusterPlusProche(
                ClusterFusionnable<ComboEquilibrage> clusterCherche,
                List<ClusterFusionnable<ComboEquilibrage>> autresClusters) {
            float minDistance = Float.MAX_VALUE;
            ClusterFusionnable<ComboEquilibrage> clusterHote = null;

            for (ClusterFusionnable<ComboEquilibrage> clusterTeste : autresClusters) {
                if (clusterTeste == clusterCherche) continue;

                float distance = distanceMoyenneClusters(clusterCherche, clusterTeste);
                if (distance < minDistance) {
                    minDistance = distance;
                    clusterHote = clusterTeste;
                }
            }

            if (clusterHote == null) throw new RuntimeException("Aucun cluster plus proche trouvé");

            return clusterHote;
        }

        /**
         * retourne la distance moyenne de tous les points entre deux clusters
         */
        private float distanceMoyenneClusters(
                ClusterFusionnable<ComboEquilibrage> cluster1, ClusterFusionnable<ComboEquilibrage> cluster2) {
            float distanceTotale = 0;
            int compte = 0;
            for (ComboEquilibrage pointCluster : cluster1.getObjets()) {
                for (ComboEquilibrage pointAutreCluster : cluster2.getObjets()) {
                    distanceTotale += distanceEquite(pointCluster, pointAutreCluster);
                    compte++;
                }
            }

            return distanceTotale / compte;
        }

        /**
         * distance moyenne entre un point et un cluster
         */
        private float distanceCluster(ComboEquilibrage point, ClusterFusionnable<ComboEquilibrage> cluster) {
            float distanceTotale = 0;
            int compte = 0;
            for (ComboEquilibrage pointAutreCluster : cluster.getObjets()) {
                if (pointAutreCluster == point) continue;
                distanceTotale += distanceEquite(point, pointAutreCluster);
                compte++;
            }

            return distanceTotale / compte;
        }

        private float distanceEquite(ComboEquilibrage point, ComboEquilibrage autrePoint) {
            return point.getEquiteFuture().distance(autrePoint.getEquiteFuture());
        }

        /**
         * renvoie le % de range d'un cluster
         */
        private float pctRange(ClusterFusionnable<ComboEquilibrage> cluster) {
            float pctRange = 0;
            for (ComboEquilibrage comboEquilibrage : cluster.getObjets()) {
                pctRange += comboEquilibrage.getPCombo();
            }

            return pctRange;
        }
    }

}
