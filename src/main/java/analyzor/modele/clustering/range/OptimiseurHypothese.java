package analyzor.modele.clustering.range;

import analyzor.modele.clustering.objets.ComboPreClustering;

import java.util.List;

/**
 * construit les hypothèses
 * puis optimise les hypothèses, les élimine jusqu'à qu'il n'en reste plus qu'une
 */
class OptimiseurHypothese {
    private static final int MAX_COMBINAISONS = 10;

    OptimiseurHypothese() {

    }

    // interface publique

    void creerHypotheses(List<ComboPreClustering> combosInitiaux) {
        HypotheseClustering.ajouterCombos(combosInitiaux);
        // on récupère le nombre de dimensions de l'ACP
        // on crée les combinaisons dont le produit est inférieur à MAX_COMBINAISONS

        // on crée les hypothèses correspondantes
    }

    List<List<ComboPreClustering>> meilleureHypothese() {
        // on choisit un pas de départ

        // on ajuste les différentes hypothèses
        // on diminue le pas au fur et à mesure et on supprime les hypothèses les moins probables

        return null;
    }
}
