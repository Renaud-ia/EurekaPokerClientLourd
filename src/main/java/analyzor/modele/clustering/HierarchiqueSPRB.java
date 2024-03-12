package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.clustering.objets.EntreeSPRB;
import analyzor.modele.clustering.objets.MinMaxCalcul;
import analyzor.modele.clustering.objets.MinMaxCalculSituation;
import analyzor.modele.parties.Entree;
import analyzor.modele.simulation.SituationStackPotBounty;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

/**
 * clustering Hierarchique selon Effective stack, Pot et pot Bounty
 */

public class HierarchiqueSPRB extends ClusteringHierarchique<EntreeSPRB> {
    MinMaxCalculSituation minMaxCalcul;

    public HierarchiqueSPRB() {
        super(MethodeLiaison.WARD);
        logger = LogManager.getLogger(HierarchiqueSPRB.class);
    }

    public void ajouterDonnees(List<Entree> donneesEntrees) {
        List<EntreeSPRB> donneesNormalisees = normaliserDonneesMinMax(donneesEntrees);

        for (EntreeSPRB entreeSPRB : donneesNormalisees) {
            entreeSPRB.activerMinMaxNormalisation(minMaxCalcul.getMinValeurs(), minMaxCalcul.getMaxValeurs());
            ClusterFusionnable<EntreeSPRB> nouveauCluster = new ClusterFusionnable<>(entreeSPRB, indexActuel++);
            clustersActuels.add(nouveauCluster);
        }
        PreClustering();
        initialiserMatrice();
    }

    private List<EntreeSPRB> normaliserDonneesMinMax(List<Entree> donneesEntrees) {
        List<EntreeSPRB> donneesTransformees = new ArrayList<>();
        for (Entree entree : donneesEntrees) {
            EntreeSPRB entreeSPRB = new EntreeSPRB(entree);
            donneesTransformees.add(entreeSPRB);
        }

        minMaxCalcul = new MinMaxCalculSituation();
        minMaxCalcul.calculerMinMax(donneesTransformees);

        return donneesTransformees;
    }

    private void PreClustering() {
        int effectifInitial = clustersActuels.size();
        logger.debug("Nombre de clusters avant préclustering : " + effectifInitial);
        List<ClusterFusionnable<EntreeSPRB>> nouveauxClusters = new ArrayList<>();

        float pasFusion = 0.01f;

        boolean[] donneeTraitee = new boolean[clustersActuels.size()];
        for (int i = 0; i < clustersActuels.size(); i++) {
            if (donneeTraitee[i]) continue;
            ClusterFusionnable<EntreeSPRB> clusterInitial = clustersActuels.get(i);
            donneeTraitee[i] = true;

            for (int j = i + 1; j < clustersActuels.size(); j++) {
                ClusterFusionnable<EntreeSPRB> nouveauCluster = clustersActuels.get(j);

                if ((clusterInitial.distance(nouveauCluster)) > pasFusion) continue;

                clusterInitial.fusionner(nouveauCluster);
                donneeTraitee[j] = true;
            }
            nouveauxClusters.add(clusterInitial);
        }
        int nombreNouveauxClusters = nouveauxClusters.size();
        clustersActuels.clear();

        for (ClusterFusionnable<EntreeSPRB> cluster : nouveauxClusters) {
            int effectif = cluster.getEffectif();
            if (effectif > (int) ((effectifInitial / nombreNouveauxClusters) * 0.1)) {
                clustersActuels.add(cluster);
            }
        }
        logger.debug("Nombre de clusters après préclustering : " + clustersActuels.size());
    }

    public List<ClusterSPRB> construireClusters(int minimumPoints) {
        // parfois on n'a qu'un seul cluster après pré-clustering
        if (clustersActuels.size() > 1) {
            logger.debug("MIN POINTS CLUSTER : " + minimumPoints);
            calculerMinEffectif();

            Integer minEffectif = 0;

            while (minEffectif < minimumPoints) {
                minEffectif = clusterSuivant();
                if (minEffectif == null) break;
            }
        }

        List<ClusterSPRB> resultats = new ArrayList<>();

        logger.debug("Nombre clusters finaux : " + clustersActuels.size());

        // on décompresse les clusters pour obtenir les résultats
        // les clusters sont sous-groupés par NoeudThéorique = action choisie
        for (ClusterFusionnable<EntreeSPRB> clusterHierarchique : clustersActuels) {
            ClusterSPRB clusterSPRB = new ClusterSPRB();
            for (EntreeSPRB entreeSPRB : clusterHierarchique.getObjets()) {
                clusterSPRB.ajouterEntree(entreeSPRB.getEntree());
            }
            clusterSPRB.clusteringTermine(
                    clusterHierarchique.getObjets().getFirst().getStacksEffectifs(),
                    clusterHierarchique.getCentroide()
                    );
            resultats.add(clusterSPRB);
        }

        return resultats;
    }

    public MinMaxCalculSituation getMinMaxCalcul() {
        return minMaxCalcul;
    }
}
