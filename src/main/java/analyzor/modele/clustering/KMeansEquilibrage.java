package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.clustering.objets.EquilibrageEquite;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

import java.util.ArrayList;
import java.util.List;

/**
 * KMeans pour équilibrage des combos
 * utilise seulement l'équité pour clusteriser
 * sert de procédure de secours si la première méthode ne marche pas
 */
public class KMeansEquilibrage extends ClusteringKMeans<EquilibrageEquite> {
    private List<EquilibrageEquite> noeuds;
    private static final int N_CLUSTERS = 3;
    public KMeansEquilibrage() {
        super();
    }

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        // on initialise
        this.noeuds = new ArrayList<>();
        for (NoeudEquilibrage noeudEquilibrage : noeuds) {
            EquilibrageEquite objetEquilibrage = new EquilibrageEquite(noeudEquilibrage);
            this.noeuds.add(objetEquilibrage);
        }
    }

    public void lancerClustering() {
        this.initialiser(noeuds);
        float inertie = this.ajusterClusters(N_CLUSTERS);

        logger.trace("INERTIE POUR " + N_CLUSTERS + "CLUSTERS : " + inertie);

        List<ClusterKMeans<EquilibrageEquite>> clusters = this.getClusters();
        int compte = 0;
        for (ClusterKMeans<EquilibrageEquite> cluster : clusters) {
            logger.trace("CLUSTER N° : " + ++compte);
            StringBuilder combosCluster = new StringBuilder();
            combosCluster.append("COMBOS : [");
            for (EquilibrageEquite comboEquilibrage : cluster.getObjets()) {
                    combosCluster.append(comboEquilibrage.getNoeud());
            }
            combosCluster.append("]");
            logger.trace(combosCluster + "\n");
        }

    }

    public List<ClusterEquilibrage> getResultats() {
        List<ClusterEquilibrage> resultats = new ArrayList<>();

        List<ClusterKMeans<EquilibrageEquite>> clusters = this.getClusters();
        for (ClusterKMeans<EquilibrageEquite> cluster : clusters) {
            List<NoeudEquilibrage> noeudsCluster = new ArrayList<>();
            for (EquilibrageEquite objetEquilibrage : cluster.getObjets()) {
                noeudsCluster.add(objetEquilibrage.getNoeud());
            }
            ClusterEquilibrage clusterEquilibrage = new ClusterEquilibrage(noeudsCluster);
            resultats.add(clusterEquilibrage);
        }

        return resultats;
    }
}
