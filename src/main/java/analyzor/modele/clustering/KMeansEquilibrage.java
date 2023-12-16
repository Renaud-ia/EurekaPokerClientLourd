package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

import java.util.ArrayList;
import java.util.List;

/**
 * KMeans pour équilibrage des combos
 * mauvais résultats
 */
@Deprecated
public class KMeansEquilibrage extends ClusteringKMeans<NoeudEquilibrage> {
    private List<NoeudEquilibrage> noeuds;
    public KMeansEquilibrage() {
        super();
    }

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        // on initialise
        this.noeuds = noeuds;
    }

    public void lancerClustering() {
        for (int i = 2; i < 100; i ++) {
            this.initialiser(noeuds);
            float inertie = this.ajusterClusters(i);

            // todo pour test à supprimer
            System.out.println("INERTIE POUR " + i + "CLUSTERS : " + inertie);

            List<ClusterKMeans<NoeudEquilibrage>> clusters = this.getClusters();
            int compte = 0;
            for (ClusterKMeans<NoeudEquilibrage> cluster : clusters) {
                System.out.println("CLUSTER N° : " + ++compte);
                StringBuilder combosCluster = new StringBuilder();
                combosCluster.append("COMBOS : [");
                for (NoeudEquilibrage comboEquilibrage : cluster.getObjets()) {
                    combosCluster.append(comboEquilibrage);
                }
                combosCluster.append("]");
                System.out.println(combosCluster);
            }

        }
    }

    public List<ClusterEquilibrage> getResultats() {
        List<ClusterEquilibrage> resultats = new ArrayList<>();

        List<ClusterKMeans<NoeudEquilibrage>> clusters = this.getClusters();
        for (ClusterKMeans<NoeudEquilibrage> cluster : clusters) {
            ClusterEquilibrage clusterEquilibrage = new ClusterEquilibrage(cluster.getObjets());
            resultats.add(clusterEquilibrage);
        }

        return resultats;
    }
}
