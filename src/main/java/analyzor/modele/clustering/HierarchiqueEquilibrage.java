package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.equilibrage.NoeudEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboDenombrable;

import java.util.ArrayList;
import java.util.List;

/**
 * méthode particulière du clustering pour équilibrage
 * on va regrouper un certain pourcentage de points
 * puis affecter tous les points isolés à un cluster déjà formé
 */
public class HierarchiqueEquilibrage extends ClusteringHierarchique<NoeudEquilibrage> {
    private final static float PCT_RANGE = 0.5f;
    private final static int MIN_EFFECTIF = 2;
    private final static MethodeLiaison METHODE_LIAISON = MethodeLiaison.WARD;
    private int nIterations;
    public HierarchiqueEquilibrage() {
        super(METHODE_LIAISON);
    }

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        nIterations = (int) (PCT_RANGE * noeuds.size());
        for (NoeudEquilibrage noeud : noeuds) {
            ClusterFusionnable<NoeudEquilibrage> nouveauCluster = new ClusterFusionnable<>(noeud, indexActuel++);
            nouveauCluster.setPoids(noeud.getPoids());
            clustersActuels.add(nouveauCluster);
        }
        initialiserMatrice();
    }

    public void lancerClustering() {
        for (int i = 0; i < nIterations; i++) {
            this.clusterSuivant();
        }
        regrouperClustersIsoles();
    }

    public List<List<ComboDenombrable>> getResultats() {
        List<List<ComboDenombrable>> resultats = new ArrayList<>();

        for (ClusterFusionnable<NoeudEquilibrage> cluster : clustersActuels) {
            List<ComboDenombrable> combosDansNoeud = new ArrayList<>();
            for (NoeudEquilibrage noeudEquilibrage : cluster.getObjets()) {
                combosDansNoeud.addAll(noeudEquilibrage.getCombosDenombrables());
            }
            resultats.add(combosDansNoeud);
        }

        return resultats;
    }

    private void regrouperClustersIsoles() {
        List<ClusterFusionnable<NoeudEquilibrage>> clustersASupprimer = new ArrayList<>();

        for (ClusterFusionnable<NoeudEquilibrage> clusterIsole : clustersActuels) {
            if (clusterIsole.getEffectif() >= MIN_EFFECTIF) continue;

            float minDistance = Float.MAX_VALUE;
            ClusterFusionnable<NoeudEquilibrage> clusterHote = clustersActuels.get(0);

            for (ClusterFusionnable<NoeudEquilibrage> clusterTest : clustersActuels) {
                if (clusterTest.getEffectif() < MIN_EFFECTIF) continue;

                float distance = strategieLiaison.calculerDistance(clusterIsole, clusterTest);
                if (distance < minDistance) {
                    minDistance = distance;
                    clusterHote = clusterTest;
                }
            }
            clusterHote.fusionner(clusterIsole);
            clustersASupprimer.add(clusterIsole);
        }

        clustersActuels.removeAll(clustersASupprimer);
    }
}
