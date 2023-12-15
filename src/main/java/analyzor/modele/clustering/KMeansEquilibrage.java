package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.equilibrage.ComboEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboDenombrable;

import java.util.ArrayList;
import java.util.List;

/**
 * KMeans pour équilibrage des combos
 * mauvais résultats
 */
@Deprecated
public class KMeansEquilibrage extends ClusteringKMeans<ComboEquilibrage> {
    private List<ComboEquilibrage> noeuds;
    public KMeansEquilibrage() {
        super();
    }

    public void ajouterDonnees(List<ComboEquilibrage> noeuds) {
        // on initialise
        this.noeuds = noeuds;
    }

    public void lancerClustering() {
        for (int i = 2; i < 100; i ++) {
            this.initialiser(noeuds);
            float inertie = this.ajusterClusters(i);

            // todo pour test à supprimer
            System.out.println("INERTIE POUR " + i + "CLUSTERS : " + inertie);

            List<ClusterKMeans<ComboEquilibrage>> clusters = this.getClusters();
            int compte = 0;
            for (ClusterKMeans<ComboEquilibrage> cluster : clusters) {
                System.out.println("CLUSTER N° : " + ++compte);
                StringBuilder combosCluster = new StringBuilder();
                combosCluster.append("COMBOS : [");
                for (ComboEquilibrage comboEquilibrage : cluster.getObjets()) {
                    for (ComboDenombrable comboDenombrable : comboEquilibrage.getCombosDenombrables()) {
                        combosCluster.append(comboDenombrable);
                        combosCluster.append(", ");
                    }
                }
                combosCluster.append("]");
                System.out.println(combosCluster);
            }

        }
    }

    public List<List<ComboDenombrable>> getResultats() {
        List<List<ComboDenombrable>> resultats = new ArrayList<>();

        List<ClusterKMeans<ComboEquilibrage>> clusters = this.getClusters();
        for (ClusterKMeans<ComboEquilibrage> cluster : clusters) {
            List<ComboDenombrable> combosDansCluster = new ArrayList<>();
            for (ComboEquilibrage comboEquilibrage : cluster.getObjets()) {
                combosDansCluster.addAll(comboEquilibrage.getCombosDenombrables());
            }
            resultats.add(combosDansCluster);
        }

        return resultats;
    }
}
