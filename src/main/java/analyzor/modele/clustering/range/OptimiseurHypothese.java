package analyzor.modele.clustering.range;

import analyzor.modele.clustering.cluster.ClusterDeBase;
import analyzor.modele.clustering.objets.ComboPreClustering;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * construit les hypothèses
 * puis optimise les hypothèses, les élimine jusqu'à qu'il n'en reste plus qu'une
 */
class OptimiseurHypothese {
    private final static Logger logger = LogManager.getLogger(OptimiseurHypothese.class);
    private static final int MAX_CLASSES_PAR_COMPOSANTE = 4;
    private static final int MIN_CLUSTERS_PAR_RANGE = 3;
    private static final int MAX_CLUSTERS_PAR_RANGE = 10;
    // plus il est élevé, plus le pas va être précis dès le début, mais plus de calcul
    // todo trouver la bonne valeur
    private static final int FACTEUR_REDUCTION_PAS = 10;
    private final List<HypotheseClustering> hypotheses;

    OptimiseurHypothese(int nSituations) {
        HypotheseClustering.setNombreObservations(nSituations);
        this.hypotheses = new ArrayList<>();
    }

    // interface publique

    void creerHypotheses(List<ComboPreClustering> combosInitiaux) {
        logger.trace("Création des hypothèses");
        // on récupère le nombre de dimensions de l'ACP
        int nComposantes = combosInitiaux.getFirst().nDimensions();
        HypotheseClustering.ajouterCombos(combosInitiaux, nComposantes);

        // on crée les combinaisons dont le produit est inférieur à MAX_COMBINAISONS
        List<List<Integer>> combinaisons = new ArrayList<>();
        genererCombinaisons(nComposantes, MAX_CLASSES_PAR_COMPOSANTE, 1, new ArrayList<>(), combinaisons);

        // on crée les hypothèses correspondantes
        for (List<Integer> valeursHypothese : combinaisons) {
            int[] hypothese = new int[valeursHypothese.size()];

            int produitCombinaisons = 1;
            for (int i = 0; i < valeursHypothese.size(); i++) {
                hypothese[i] = valeursHypothese.get(i);
                produitCombinaisons *= valeursHypothese.get(i);
            }

            if (produitCombinaisons > MAX_CLUSTERS_PAR_RANGE || produitCombinaisons < MIN_CLUSTERS_PAR_RANGE) continue;

            HypotheseClustering nouvelleHypothese = new HypotheseClustering(hypothese);
            this.hypotheses.add(nouvelleHypothese);

            logger.trace("Hypothèse ajoutée : " + Arrays.toString(hypothese));
        }

    }

    List<ClusterDeBase<ComboPreClustering>> meilleureHypothese() {
        // on choisit un pas de départ
        float pas = 1f;
        while(hypotheses.size() > 1) {
            // on diminue le pas au fur et à mesure
            pas = actualiserPas(pas);

            // on ajuste les différentes hypothèses
            ajusterHypotheses();

            // on supprime les hypothèses les moins probables
            supprimerMoinsBonneHypothese();

            logger.trace("Ajustement terminé avec pas : " + pas);
        }

        logger.trace("Plus qu'une hypothèse restante");

        return hypotheses.getFirst().clusteringActuel();
    }

    private void ajusterHypotheses() {
        int nAjustements = FACTEUR_REDUCTION_PAS - 1;
        HypotheseClustering.setNombreAjustements(nAjustements);

        // todo on peut multiprocesser facilement
        for (HypotheseClustering hypotheseClustering : hypotheses) {
            hypotheseClustering.ajusterValeurs();
        }
    }



    private void supprimerMoinsBonneHypothese() {
        float coutPlusEleve = Float.MIN_VALUE;
        HypotheseClustering pireHypothese = null;

        for (HypotheseClustering hypotheseClustering : hypotheses) {
            float coutHypothese = hypotheseClustering.coutAjustementActuel();
            if (coutHypothese > coutPlusEleve) {
                coutPlusEleve = coutHypothese;
                pireHypothese = hypotheseClustering;
            }
        }

        if (pireHypothese == null) throw new RuntimeException("Aucune pire hypothèse trouvée");
        hypotheses.remove(pireHypothese);
    }


    private float actualiserPas(float pas) {
        float nouveauPas = pas / FACTEUR_REDUCTION_PAS;
        HypotheseClustering.setPas(nouveauPas);
        return nouveauPas;
    }


    // méthodes privées

    /**
     * génère de manière récursive les combinaisons d'hypothèses possibles
     * = matrice euclidienne des possibilités
     * @param nComposantes : nombre de composantes
     * @param MAX_VALEUR : nombre max de classes par composante (le max sera inclus)
     * @param currentVariable : nombre minimum de classes par composante
     * @param currentCombination : paramètre pour la récursivité (initialiser avec une liste vide)
     * @param combinations : liste à remplir avec les combinaisons possibles
     */
    private static void genererCombinaisons(
            int nComposantes,
            int MAX_VALEUR,
            int currentVariable,
            List<Integer> currentCombination,
            List<List<Integer>> combinations) {

        // Si toutes les variables ont été attribuées une valeur, ajouter la combinaison à la liste
        if (currentVariable > nComposantes) {
            combinations.add(new ArrayList<>(currentCombination));
            return;
        }

        // Pour chaque valeur possible de la variable actuelle
        for (int value = 1; value <= MAX_VALEUR; value++) {
            // Attribuer la valeur à la variable actuelle
            currentCombination.add(value);
            // Générer les combinaisons pour les variables suivantes
            genererCombinaisons(nComposantes, MAX_VALEUR,
                    currentVariable + 1, currentCombination, combinations);
            // Retirer la valeur de la variable actuelle pour tester les autres valeurs
            currentCombination.removeLast();
        }
    }
}
