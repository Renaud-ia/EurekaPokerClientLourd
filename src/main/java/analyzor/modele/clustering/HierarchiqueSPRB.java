package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.clustering.objets.EntreeSPRB;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.HashMap;
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
        PreClustering();
        initialiserMatrice();
    }

    private void PreClustering() {
        int effectifInitial = clustersActuels.size();
        System.out.println("Nombre de clusters avant préclustering : " + effectifInitial);
        List<ClusterHierarchique<EntreeSPRB>> nouveauxClusters = new ArrayList<>();

        float pasStack = 0.5f;
        float pasPot = 0.5f;
        float pasPotBounty = 0.2f;

        boolean[] donneeTraitee = new boolean[clustersActuels.size()];
        for (int i = 0; i < clustersActuels.size(); i++) {
            if (donneeTraitee[i]) continue;
            ClusterHierarchique<EntreeSPRB> clusterInitial = clustersActuels.get(i);
            donneeTraitee[i] = true;

            float stackEffectif = clusterInitial.getCentroide()[0];
            float pot = clusterInitial.getCentroide()[1];
            float potBounty = clusterInitial.getCentroide()[2];

            for (int j = i + 1; j < clustersActuels.size(); j++) {
                ClusterHierarchique<EntreeSPRB> nouveauCluster = clustersActuels.get(j);

                if (Math.abs(stackEffectif - nouveauCluster.getCentroide()[0]) > pasStack) continue;
                if (Math.abs(pot - nouveauCluster.getCentroide()[1]) > pasPot) continue;
                if (potBounty > 0) {
                    if ((Math.abs(potBounty - nouveauCluster.getCentroide()[2]) / potBounty) > pasPotBounty) continue;
                }

                clusterInitial.fusionner(nouveauCluster);
                donneeTraitee[j] = true;
            }
            nouveauxClusters.add(clusterInitial);
        }
        int nombreNouveauxClusters = nouveauxClusters.size();
        clustersActuels.clear();

        for (ClusterHierarchique<EntreeSPRB> cluster : nouveauxClusters) {
            int effectif = cluster.getEffectif();
            if (effectif > (int) ((effectifInitial / nombreNouveauxClusters) * 0.1)) {
                clustersActuels.add(cluster);
            }
        }
        System.out.println("Nombre de clusters après préclustering : " + clustersActuels.size());
    }

    @Override
    public List<ClusterSPRB> construireClusters(int minimumPoints) {
        System.out.println("MIN POINTS CLUSTER : " + minimumPoints);
        calculerMinEffectif();
        List<ClusterSPRB> resultats = new ArrayList<>();

        Integer minEffectif = 0;

        while(minEffectif < minimumPoints) {
            minEffectif = clusterSuivant();
            if (minEffectif == null) break;
        }

        System.out.println("Nombre clusters finaux : " + clustersActuels.size());

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
