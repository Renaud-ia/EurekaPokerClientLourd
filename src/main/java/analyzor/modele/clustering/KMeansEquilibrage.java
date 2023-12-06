package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.equilibrage.NoeudEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboDenombrable;

import java.util.ArrayList;
import java.util.List;

/**
 * KMeans pour équilibrage des combos
 */
public class KMeansEquilibrage extends ClusteringKMeans<NoeudEquilibrage> {
    private List<NoeudEquilibrage> noeuds;
    public KMeansEquilibrage() {
        super();
    }

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        // on fixe le poids
        this.poids = noeuds.get(0).getPoids();

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
                for (NoeudEquilibrage noeudEquilibrage : cluster.getObjets()) {
                    for (ComboDenombrable comboDenombrable : noeudEquilibrage.getCombosDenombrables()) {
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

        List<ClusterKMeans<NoeudEquilibrage>> clusters = this.getClusters();
        for (ClusterKMeans<NoeudEquilibrage> cluster : clusters) {
            List<ComboDenombrable> combosDansCluster = new ArrayList<>();
            for (NoeudEquilibrage noeudEquilibrage : cluster.getObjets()) {
                combosDansCluster.addAll(noeudEquilibrage.getCombosDenombrables());
            }
            resultats.add(combosDansCluster);
        }

        return resultats;
    }
}
