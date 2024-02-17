package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.cluster.ClusterDeBase;
import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.clustering.range.AcpRange;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * méthode particulière du clustering pour équilibrage
 * on va regrouper un certain pourcentage de points
 * puis affecter tous les points isolés à un cluster déjà formé
 * peut prendre des ComboIsole, ComboDansCluster ou bien des ClusterEquilibrage
 */
public class HierarchiqueRange extends ClusteringHierarchique<ComboPreClustering> {
    // pct de range sur lesquels sont calculés les clusters de densité initiaux
    private final static float PCT_RANGE = 0.6f;
    //min de % de range pour prendre en compte un cluster
    private final static float MIN_PCT_RANGE = 0.02f;
    private final static int MIN_EFFECTIF_CLUSTER = 200;
    private final static MethodeLiaison METHODE_LIAISON = MethodeLiaison.MOYENNE;
    private int nClustersFinaux;
    private final int nSituations;
    public HierarchiqueRange(int nSituations) {
        super(METHODE_LIAISON);
        logger = LogManager.getLogger(HierarchiqueRange.class);
        this.nSituations = nSituations;
    }

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        this.nClustersFinaux = 5;

        AcpRange acpRange = new AcpRange();
        acpRange.ajouterDonnees(noeuds);
        acpRange.transformer();
        // important, il nous faut un nouveau type d'objet car il stocke les valeurs transformées par l'ACP
        List<ComboPreClustering> donneesTransformees = acpRange.getDonnesTransformees();

        super.construireClustersDeBase(donneesTransformees);
    }

    public void lancerClustering() {
        while (clustersActuels.size() > nClustersFinaux){
            this.clusterSuivant();
        }
        logger.trace("Clustering hiérarchique terminé");
        // todo pour débug à supprimer
        for (ClusterFusionnable<ComboPreClustering> cluster : clustersActuels) {
            logger.debug("###CLUSTER FORME###");
            for (ComboPreClustering comboEquilibrage : cluster.getObjets()) {
                logger.trace(comboEquilibrage);
                //logger.trace(Arrays.toString(comboEquilibrage.getStrategieActuelle()));
            }
        }
    }

    /**
     * utilisé pour préclustering test
     */
    List<ClusterDeBase<ComboPreClustering>> obtenirClusters() {
        return new ArrayList<>(clustersActuels);
    }

    public List<ClusterEquilibrage> getResultats() {
        CorrectionClusters correctionClusters =
                new CorrectionClusters((float) MIN_EFFECTIF_CLUSTER / nSituations);
        correctionClusters.ajouterCentres(clustersActuels);

        // on crée des noeuds Equilibrage à partir des clusters corrigés
        List<ClusterEquilibrage> resultats = new ArrayList<>();

        for (ClusterFusionnable<ComboPreClustering> cluster : correctionClusters.recupererClusters()) {
            List<NoeudEquilibrage> noeudsClusters = new ArrayList<>();
            logger.debug("###CLUSTER FORME###");
            for (ComboPreClustering comboEquilibrage : cluster.getObjets()) {
                logger.trace(comboEquilibrage);
                //logger.trace(Arrays.toString(comboEquilibrage.getStrategieActuelle()));
                noeudsClusters.add(comboEquilibrage.getNoeudEquilibrage());
            }
            ClusterEquilibrage noeudParent = new ClusterEquilibrage(noeudsClusters);
            resultats.add(noeudParent);
        }

        return resultats;
    }


    /**
     * classe qui va corriger les clusters de densité préalablement formés
     * ne travaille qu'en terme d'équité brute
     */
    static class CorrectionClusters {
        private List<ClusterFusionnable<ComboPreClustering>> clustersGroupes;
        private final float minPctCluster;
        CorrectionClusters(float minPctCluster) {
            clustersGroupes = new ArrayList<>();
            this.minPctCluster = minPctCluster;
        }

        void ajouterCentres(List<ClusterFusionnable<ComboPreClustering>> clusters) {
            clustersGroupes = clusters;
            reaffecterIntrus();
        }

        List<ClusterFusionnable<ComboPreClustering>> recupererClusters() {
            return clustersGroupes;
        }

        /**
         * on prend la distance moyenne d'un point avec les autres points de son cluster
         * et on compare avec la distance moyenne des autres points d'un autre cluster
         */
        private void reaffecterIntrus() {
            logger.debug("Réaffectation des intrus");
            for (ClusterFusionnable<ComboPreClustering> clusterInitial : clustersGroupes) {
                // on crée un itérateur pour pouvoir remove pendant qu'on loop
                Iterator<ComboPreClustering> iterateurCluster = clusterInitial.getObjets().iterator();
                while(iterateurCluster.hasNext()) {
                    ClusterFusionnable<ComboPreClustering> clusterAccueil = null;
                    ComboPreClustering pointCluster = iterateurCluster.next();
                    float distanceInitiale = distanceCluster(pointCluster, clusterInitial);
                    float minAutreDistance = Float.MAX_VALUE;
                    for (ClusterFusionnable<ComboPreClustering> clusterHote : clustersGroupes) {
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

        private void fusionnerClustersTropPetits() {
            Iterator<ClusterFusionnable<ComboPreClustering>> iterateurClusters = clustersGroupes.iterator();
            while(iterateurClusters.hasNext()) {
                ClusterFusionnable<ComboPreClustering> clusterTraite = iterateurClusters.next();
                // deux critères : x combos servis et en % de range
                if (pctRange(clusterTraite) >= minPctCluster && pctRange(clusterTraite) > MIN_PCT_RANGE) continue;
                logger.trace("Cluster trop petit, on le fusionne : " + clusterTraite);

                ClusterFusionnable<ComboPreClustering> clusterHote = clusterPlusProche(clusterTraite, clustersGroupes);
                clusterHote.fusionner(clusterTraite);
                iterateurClusters.remove();
            }
        }

        private ClusterFusionnable<ComboPreClustering> clusterPlusProche(
                ClusterFusionnable<ComboPreClustering> clusterCherche,
                List<ClusterFusionnable<ComboPreClustering>> autresClusters) {
            float minDistance = Float.MAX_VALUE;
            ClusterFusionnable<ComboPreClustering> clusterHote = null;

            for (ClusterFusionnable<ComboPreClustering> clusterTeste : autresClusters) {
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
                ClusterFusionnable<ComboPreClustering> cluster1, ClusterFusionnable<ComboPreClustering> cluster2) {
            float distanceTotale = 0;
            int compte = 0;
            for (ComboPreClustering pointCluster : cluster1.getObjets()) {
                for (ComboPreClustering pointAutreCluster : cluster2.getObjets()) {
                    distanceTotale += distanceEquite(pointCluster, pointAutreCluster);
                    compte++;
                }
            }

            return distanceTotale / compte;
        }

        /**
         * distance moyenne entre un point et un cluster
         */
        private float distanceCluster(ComboPreClustering point, ClusterFusionnable<ComboPreClustering> cluster) {
            float distanceTotale = 0;
            int compte = 0;
            for (ComboPreClustering pointAutreCluster : cluster.getObjets()) {
                if (pointAutreCluster == point) continue;
                distanceTotale += distanceEquite(point, pointAutreCluster);
                compte++;
            }

            return distanceTotale / compte;
        }

        private float distanceEquite(ComboPreClustering point, ComboPreClustering autrePoint) {
            return point.getEquiteFuture().distance(autrePoint.getEquiteFuture());
        }

        /**
         * renvoie le % de range d'un cluster
         */
        private float pctRange(ClusterFusionnable<ComboPreClustering> cluster) {
            float pctRange = 0;
            for (ComboPreClustering comboEquilibrage : cluster.getObjets()) {
                pctRange += comboEquilibrage.getPCombo();
            }

            return pctRange;
        }
    }

}
