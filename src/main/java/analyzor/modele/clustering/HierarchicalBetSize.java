package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.objets.EntreeBetSize;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalBetSize extends ClusteringHierarchique<EntreeBetSize> implements ClusteringEntreeMinEffectif {

    public HierarchicalBetSize() {
        super(MethodeLiaison.WARD);
    }

    @Override
    public void ajouterDonnees(List<Entree> donneesEntrees) {
        for (Entree entree : donneesEntrees) {
            EntreeBetSize entreeBetSize = new EntreeBetSize(entree);
            ClusterFusionnable<EntreeBetSize> nouveauCluster = new ClusterFusionnable<>(entreeBetSize, indexActuel++);
            clustersActuels.add(nouveauCluster);
        }
        regrouperDoublons();
        initialiserMatrice();
    }

    private void regrouperDoublons() {
        System.out.println("Nombre de clusters avant préclustering : " + clustersActuels.size());
        List<ClusterFusionnable<EntreeBetSize>> nouveauxClusters = new ArrayList<>();
        nouveauxClusters.add(clustersActuels.get(0));

        for (ClusterFusionnable<EntreeBetSize> clusterInitial : clustersActuels) {
            boolean clusterFusionne = false;
            for (ClusterFusionnable<EntreeBetSize> nouveauCluster : nouveauxClusters) {
                float distanceBetSize =
                        Math.abs(nouveauCluster.getCentroide()[0] - clusterInitial.getCentroide()[0]);

                if (distanceBetSize < 0.001) {
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
    public List<ClusterBetSize> construireClusters(int minimumPoints) {
        this.calculerMinEffectif();
        List<ClusterBetSize> resultats = new ArrayList<>();

        Integer minEffectif = clusterSuivant();
        if (minEffectif == null) return null;

        while(minEffectif < minimumPoints) {
            minEffectif = clusterSuivant();
            if (minEffectif == null) break;
        }

        System.out.println("Nombre de clusters BetSize : " + clustersActuels.size());

        // on décompresse les clusters pour obtenir les résultats
        // les clusters sont sous-groupés par NoeudThéorique = action choisie
        for (ClusterFusionnable<EntreeBetSize> clusterHierarchique : clustersActuels) {
            ClusterBetSize clusterBetSize = new ClusterBetSize();
            for (EntreeBetSize entreeBetSize : clusterHierarchique.getObjets()) {
                clusterBetSize.ajouterEntree(entreeBetSize.getEntree());
            }
            clusterBetSize.setBetSize(clusterHierarchique.getCentroide()[0]);
            resultats.add(clusterBetSize);
        }

        return resultats;
    }
}
