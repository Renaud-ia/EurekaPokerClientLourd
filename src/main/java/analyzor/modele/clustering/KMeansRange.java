package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterDeBase;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.clustering.objets.MinMaxCalcul;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * clustering qui sert à trouver les centres de gravité initiaux
 * choisit le nombre de clusters adaptés
 */
public class KMeansRange extends ClusteringKMeans<ComboPreClustering> {
    private final static int N_OBSERVATIONS_PAR_CLUSTER = 300;
    private final static int MIN_N_CLUSTERS = 3;
    private final static int MAX_N_CLUSTERS = 9;
    private final int nSituations;
    KMeansRange(int nSituations) {
        super();
        this.nSituations = nSituations;
    }

    void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        // on affecte un poids equite/proba observatios en fonction du nombre de situations / noeuds moyen
        final float poidsRelatifEquiteObservations = poidsRelatif((float) nSituations / noeuds.size());
        formaterDonnees(noeuds, poidsRelatifEquiteObservations);
    }

    /**
     * mise en forme des données de départ
     * @param noeuds comboInitiaux
     * @param poidsRelatifEquiteObservations poids relatif équite/observations
     */
    private void formaterDonnees(List<NoeudEquilibrage> noeuds, float poidsRelatifEquiteObservations) {
        // on crée des objets spéciaux qui implémentent les bonnes méthodes
        LinkedList<ComboPreClustering> comboPreClusterings = new LinkedList<>();
        for (NoeudEquilibrage noeudEquilibrage : noeuds) {
            ComboPreClustering combo = new ComboPreClustering(noeudEquilibrage);
            comboPreClusterings.add(combo);
        }

        MinMaxCalcul<ComboPreClustering> minMaxCalcul = new MinMaxCalcul<>();
        minMaxCalcul.calculerMinMax(0, Float.MIN_VALUE, comboPreClusterings);

        // on normalise les données avec min max
        // on calcule les valeurs min et max
        float[] minValeurs = minMaxCalcul.getMinValeurs();
        float[] maxValeurs = minMaxCalcul.getMaxValeurs();

        for (ComboPreClustering comboEquilibrage : comboPreClusterings) {
            comboEquilibrage.activerMinMaxNormalisation(minValeurs, maxValeurs);
        }

        AcpRange acpRange = new AcpRange();
        acpRange.ajouterDonnees(comboPreClusterings);
        acpRange.transformer();
        List<ComboPreClustering> donneesTransformees = acpRange.getDonnesTransformees();

        super.initialiser(donneesTransformees);
    }

    /**
     * décide du nombre de clusters nécessaires
     * puis lance le clustering
     */
    public void lancerClustering() {
        final int n_clusters = Math.min(
                Math.max(nSituations / N_OBSERVATIONS_PAR_CLUSTER, MIN_N_CLUSTERS), MAX_N_CLUSTERS);

        super.ajusterClusters(n_clusters);
    }

    List<ClusterDeBase<ComboPreClustering>> getCentresGravites() {
        return new ArrayList<>(super.getClusters());
    }


    /**
     * détermine le poids relatif de l'équité et des observations lors du préclustering
     * moins on a de données, plus on va se baser sur l'équité donc valeur élevée
     * @param nSituationsParCombo le nombre moyens de situations par combo
     * @return le poids relatif Equite/Observations
     */
    private float poidsRelatif(float nSituationsParCombo) {
        // todo trouver de bonnes valeurs
        if (nSituationsParCombo > 100) return 3f;
        if (nSituationsParCombo > 50) return 5f;
        else return 10f;
    }
}
