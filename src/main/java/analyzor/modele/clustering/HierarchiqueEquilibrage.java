package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.equilibrage.NoeudEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboDenombrable;
import analyzor.modele.poker.evaluation.EquiteFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * méthode particulière du clustering pour équilibrage
 * on va regrouper un certain pourcentage de points
 * puis affecter tous les points isolés à un cluster déjà formé
 */
public class HierarchiqueEquilibrage extends ClusteringHierarchique<NoeudEquilibrage> {
    private final static float PCT_RANGE = 0.75f;
    //todo déterminer de manière dynamique ??
    private final static int MIN_EFFECTIF = 3;
    private final static MethodeLiaison METHODE_LIAISON = MethodeLiaison.WARD;
    private int nIterations;
    public HierarchiqueEquilibrage() {
        super(METHODE_LIAISON);
        logger = LogManager.getLogger(HierarchiqueEquilibrage.class);
    }

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        nIterations = (int) (PCT_RANGE * noeuds.size());

        normaliserDonnees(noeuds);

        for (NoeudEquilibrage noeud : noeuds) {
            ClusterFusionnable<NoeudEquilibrage> nouveauCluster = new ClusterFusionnable<>(noeud, indexActuel++);
            nouveauCluster.setPoids(noeud.getPoids());
            clustersActuels.add(nouveauCluster);
        }
        initialiserMatrice();
    }

    public void lancerClustering() {
        // todo on pourrait prendre en compte l'inertie et sa variation
        for (int i = 0; i < nIterations; i++) {
            this.clusterSuivant();
        }
    }

    public List<NoeudEquilibrage> getResultats() {
        List<NoeudEquilibrage> resultats = new ArrayList<>();

        List<ClusterFusionnable<NoeudEquilibrage>> clustersASupprimer = new ArrayList<>();

        for (ClusterFusionnable<NoeudEquilibrage> cluster : clustersActuels) {
            if (cluster.getEffectif() < MIN_EFFECTIF) continue;
            logger.debug("###CLUSTER FORME###");
            List<ComboDenombrable> combosDansNoeud = new ArrayList<>();
            for (NoeudEquilibrage noeudEquilibrage : cluster.getObjets()) {
                logger.trace(noeudEquilibrage);
                logger.trace(Arrays.toString(noeudEquilibrage.getStrategie()));
                combosDansNoeud.addAll(noeudEquilibrage.getCombosDenombrables());
            }
            NoeudEquilibrage noeudParent = new NoeudEquilibrage(combosDansNoeud);
            resultats.add(noeudParent);

            clustersASupprimer.add(cluster);
        }

        clustersActuels.removeAll(clustersASupprimer);
        regrouperClustersIsoles(resultats);

        return resultats;
    }

    private void regrouperClustersIsoles(List<NoeudEquilibrage> resultats) {
        // il ne reste que les clusters avec pas assez de combos
        for (ClusterFusionnable<NoeudEquilibrage> clusterIsole : clustersActuels) {
            float minDistance = Float.MAX_VALUE;
            EquiteFuture equiteClusterIsole = getEquite(clusterIsole);
            NoeudEquilibrage clusterHote = null;

            logger.trace("CLUSTER ORPHELIN : " + clusterIsole.getObjets().get(0));

            for (NoeudEquilibrage noeudEquilibrage : resultats) {
                // attention combos non fiables => on ne regarde que l'équité pour associer des clusters
                float distance = equiteClusterIsole.distance(noeudEquilibrage.getEquiteFuture());
                if (distance < minDistance) {
                    minDistance = distance;
                    clusterHote = noeudEquilibrage;
                }
            }
            if (clusterHote == null) throw new RuntimeException("Aucun cluster plus proche trouvé");

            logger.trace("CLUSTER ASSOCIE : " + clusterHote);

            for (NoeudEquilibrage noeudAjoute : clusterIsole.getObjets()) {
                for (ComboDenombrable comboAjoute : noeudAjoute.getCombosDenombrables()) {
                    clusterHote.ajouterCombo(comboAjoute);
                }
            }
        }
    }

    private EquiteFuture getEquite(ClusterFusionnable<NoeudEquilibrage> clusterIsole) {
        // todo fait doublon avec méthode dans NoeudEquilibrage : comment refactoriser???
        List<EquiteFuture> equites = new ArrayList<>();
        List<Float> poids = new ArrayList<>();

        for (NoeudEquilibrage noeudAjoute : clusterIsole.getObjets()) {
            for (ComboDenombrable comboAjoute : noeudAjoute.getCombosDenombrables()) {
                equites.add(comboAjoute.getEquiteFuture());
                poids.add(comboAjoute.getPCombo());
            }
        }

        return new EquiteFuture(equites, poids);
    }

    private void normaliserDonnees(List<NoeudEquilibrage> noeuds) {
        // on calcule les valeurs min et max
        float[] minValeurs = new float[noeuds.get(0).valeursClusterisables().length];
        Arrays.fill(minValeurs, Float.MIN_VALUE);
        float[] maxValeurs = new float[noeuds.get(0).valeursClusterisables().length];
        Arrays.fill(maxValeurs, Float.MAX_VALUE);

        for (int i = 0; i < minValeurs.length; i++) {
            float minValeur = Float.MAX_VALUE;
            float maxValeur = Float.MIN_VALUE;

            for (NoeudEquilibrage noeudEquilibrage : noeuds) {
                if (noeudEquilibrage.valeursClusterisables()[i] > minValeur) {
                    minValeur = noeudEquilibrage.valeursClusterisables()[i];
                }
                if (noeudEquilibrage.valeursClusterisables()[i] < maxValeur) {
                    maxValeur = noeudEquilibrage.valeursClusterisables()[i];
                }
            }
            minValeurs[i] = minValeur;
            maxValeurs[i] = maxValeur;
        }

        for (NoeudEquilibrage noeudEquilibrage : noeuds) {
            noeudEquilibrage.activerLogNormalisation();
            noeudEquilibrage.activerMinMaxNormalisation(minValeurs, maxValeurs);
        }
    }
}
