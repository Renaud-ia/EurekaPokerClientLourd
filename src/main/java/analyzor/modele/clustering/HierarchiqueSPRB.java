package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.clustering.objets.EntreeSPRB;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

/**
 * clustering Hierarchique selon Effective stack, Pot et pot Bounty
 */

public class HierarchiqueSPRB extends ClusteringHierarchique<EntreeSPRB> implements ClusteringEntreeMinEffectif {

    public HierarchiqueSPRB() {
        super(MethodeLiaison.WARD);
    }

    public void ajouterDonnees(List<Entree> donneesEntrees) {
        for (Entree entree : donneesEntrees) {
            EntreeSPRB entreeSPRB = new EntreeSPRB(entree);
            ClusterHierarchique<EntreeSPRB> nouveauCluster = new ClusterHierarchique<>(entreeSPRB, indexActuel++);
            clustersActuels.add(nouveauCluster);
        }
        regrouperDoublons();
        initialiserMatrice();
    }

    // on va regrouper les clusters initiaux dont l'écart est très faible = points identiques
    private void regrouperDoublons() {
        System.out.println("Nombre de clusters avant préclustering : " + clustersActuels.size());
        List<ClusterHierarchique<EntreeSPRB>> nouveauxClusters = new ArrayList<>();
        nouveauxClusters.add(clustersActuels.get(0));

        for (ClusterHierarchique<EntreeSPRB> clusterInitial : clustersActuels) {
            boolean clusterFusionne = false;
            for (ClusterHierarchique<EntreeSPRB> nouveauCluster : nouveauxClusters) {
                float distanceStackEffectif =
                        Math.abs(nouveauCluster.getCentroide()[0] - clusterInitial.getCentroide()[0]);
                float distancePot =
                        Math.abs(nouveauCluster.getCentroide()[1] - clusterInitial.getCentroide()[1]);
                float distancePotBounty =
                        Math.abs(nouveauCluster.getCentroide()[2] - clusterInitial.getCentroide()[2]);

                if (distanceStackEffectif < 0.001 && distancePot < 0.001 && distancePotBounty < 0.001) {
                    nouveauCluster.fusionner(clusterInitial);
                    clusterFusionne = true;
                    break;
                }
            }
            if (!clusterFusionne) nouveauxClusters.add(clusterInitial);
        }
        clustersActuels.clear();
        clustersActuels.addAll(nouveauxClusters);
        System.out.println("Nombre de clusters après préclustering : " + clustersActuels.size());
    }

    @Override
    public List<ClusterSPRB> construireClusters(int minimumPoints) {
        this.setMinimumPoints(minimumPoints);
        List<ClusterSPRB> resultats = new ArrayList<>();

        Integer minEffectif = 0;

        while(minEffectif < minimumPoints) {
            minEffectif = clusterSuivant();
            if (minEffectif == null) break;
        }

        // on décompresse les clusters pour obtenir les résultats
        // les clusters sont sous-groupés par NoeudThéorique = action choisie
        for (ClusterHierarchique<EntreeSPRB> clusterHierarchique : clustersActuels) {
            ClusterSPRB clusterSPRB = new ClusterSPRB();
            for (EntreeSPRB entreeSPRB : clusterHierarchique.getObjets()) {
                clusterSPRB.ajouterEntree(entreeSPRB.getEntree());
            }
            clusterSPRB.setStackEffectif(clusterHierarchique.getCentroide()[0]);
            clusterSPRB.setPot(clusterHierarchique.getCentroide()[1]);
            clusterSPRB.setPotBounty(clusterHierarchique.getCentroide()[2]);
            resultats.add(clusterSPRB);
        }

        return resultats;
    }
}
