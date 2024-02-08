package analyzor.modele.clustering;

import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

import java.util.List;

/**
 * méthode innovante pour clusteriser les ranges
 * on essaie d'identifier des centres de gravité stratégiques de la range
 * tient compte du nombre de combos dans la range
 *
 *
 */
public class SpecialRange {
    private final int nSituations;
    public SpecialRange(int nSituations) {
        this.nSituations = nSituations;
    }

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        // on définit le nombre de centres de gravité
    }

    public void lancerClustering() {
        // on clusterise la range par KMeans sur équité future

        // on prend les points les plus représentatifs en termes de probabilités (=stratégie)
        // distance moyenne avec les autres points => centres de densité locaux moins sensibles aux valeurs aberrantes
        // on pourrait également prendre les centroides mais pas forcément pertinent

        // on les étend par équité future jusqu'à arriver aux frontières des clusters

        // pour les points limites, on prend équite ET probabilités
    }

    public List<ClusterEquilibrage> getResultats() {
        return null;
    }
}
