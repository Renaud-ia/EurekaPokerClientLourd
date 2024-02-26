package analyzor.modele.clustering.range;

import analyzor.modele.clustering.cluster.ClusterDeBase;
import analyzor.modele.clustering.cluster.ClusterRange;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.utils.Bits;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * construit les hypothèses, lance leur optimisation
 * à la fin choisit la meilleure
 */
class OptimiseurHypothese {
    private final static Logger logger = LogManager.getLogger(OptimiseurHypothese.class);
    private static final int MAX_CLASSES_PAR_COMPOSANTE = 4;
    private static final int MIN_CLUSTERS_PAR_RANGE = 3;
    private static final int MAX_CLUSTERS_PAR_RANGE = 12;
    private static final float N_SERVIS_MINIMAL = 50;
    // valeur optimale qu'on veut => au dessus ne change plus rien
    private static final float N_SERVIS_OPTIMAL = 500;
    // plus il est élevé, plus le pas va être précis dès le début, mais plus de calcul
    // todo trouver la bonne valeur
    private static final int FACTEUR_REDUCTION_PAS = 10;
    private final List<HypotheseClustering> hypotheses;
    private final int nObservations;

    OptimiseurHypothese(int nSituations) {
        HypotheseClustering.setNombreObservations(nSituations);
        this.nObservations = nSituations;
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

    /**
     * interface publique pour lancer l'algo et retrouver la meilleure hypothèse
     * @return la meilleure hypothèse avec les centres de gravité retenus
     */
    List<ComboPreClustering> meilleureHypothese() {
        // on choisit un pas de départ
        float pas = 1f;
        int tour = 0;
        while(tour ++ < 5) {
            logger.debug("Tour d'actualisation");
            // on diminue le pas au fur et à mesure
            pas = actualiserPas(pas);

            // on ajuste les différentes hypothèses
            ajusterHypotheses();

            // on supprime les hypothèses les moins probables
            //supprimerMoinsBonneHypothese();

            logger.trace("Ajustement terminé avec pas : " + pas);
        }

        logger.debug("Ajustement terminé");

        return selectionnerMeilleureHypothese();
    }

    // méthodes privées

    /**
     * trouve l'hypothèse la mailleure selon ses critères
     * @return les centres de gravité associés à cette hypothès
     */
    private List<ComboPreClustering> selectionnerMeilleureHypothese() {
        float coutPlusEleve = Float.MIN_VALUE;
        HypotheseClustering meilleureHypothese = null;

        for (HypotheseClustering hypotheseClustering : hypotheses) {
            float coutHypothese = qualiteHypothese(hypotheseClustering);
            if (coutHypothese > coutPlusEleve) {
                coutPlusEleve = coutHypothese;
                meilleureHypothese = hypotheseClustering;
            }

            logger.trace("Hypothèse : " + hypotheseClustering.clusteringActuel());
            logger.trace("Cout : " + coutHypothese);
        }

        if (meilleureHypothese == null) throw new RuntimeException("Aucune meilleure hypothèse trouvée");
        logger.debug("Meilleure hypothèse : " + meilleureHypothese.clusteringActuel());

        return isolerCentres(meilleureHypothese.clusteringActuel());
    }

    /**
     * attribue un score à chaque hypothèse basé sur (par ordre d'importance)
     * - le nombre de combos servis dans le plus petit cluster (cherche à atteindre un minimum)
     * - le nombre de clusters (favorise un grand nombre)
     * - la distance inter-cluster
     * plus le score est important mieux c'est
     */
    private float qualiteHypothese(HypotheseClustering hypotheseClustering) {
        return ((hypotheseClustering.clusteringActuel().size()
                << 1) +
                distanceInterCluster(hypotheseClustering)) *
                penaliteMinServis(hypotheseClustering);
    }

    /**
     * mesure la distance intercluster globale
     * mappe la valeur entre 0 et 1
     * plus c'est important mieux c'est
     * @param hypotheseClustering hypothèse qu'on veut mesurer
     * @return le score entre 0 et 1
     */
    private int distanceInterCluster(HypotheseClustering hypotheseClustering) {
        // todo : trouver une mesure entre 0 et 1
        // par ex la plus grande distance inter-cluster / la plus grande distance inter-combo
        // en terme d'équité
        for (int i = 0; i < hypotheseClustering.nClusters(); i++) {
            for (int j = i + 1; i < hypotheseClustering.nClusters(); j++) {

            }
        }
        return 0;
    }

    private float penaliteMinServis(HypotheseClustering hypotheseClustering) {
        int minServis = Integer.MAX_VALUE;

        // on trouve le cluster avec le moins de combos servis
        for (ClusterDeBase<ComboPreClustering> cluster : hypotheseClustering.clusteringActuel()) {
            float nCombosServis = 0f;
            for (ComboPreClustering combo : cluster.getObjets()) {
                nCombosServis += combo.getPCombo() * nObservations;
            }

            if ((int) nCombosServis < minServis) {
                minServis = (int) nCombosServis;
            }
        }

        InverseSigmoidFunction inverseSigmoidFunction = new InverseSigmoidFunction(N_SERVIS_MINIMAL, N_SERVIS_OPTIMAL);
        return (float) inverseSigmoidFunction.getValeur(minServis);
    }

    private List<ComboPreClustering> isolerCentres(List<ClusterDeBase<ComboPreClustering>> clustersFormes) {
        List<ComboPreClustering> centresIsoles = new ArrayList<>();

        for (ClusterDeBase<ComboPreClustering> cluster : clustersFormes) {
            centresIsoles.add(cluster.getCentreCluster());
        }

        return centresIsoles;
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
        logger.debug("Hypothèse supprimée : " + pireHypothese.clusteringActuel());
        hypotheses.remove(pireHypothese);
    }


    private float actualiserPas(float pas) {
        float nouveauPas = pas / FACTEUR_REDUCTION_PAS;
        HypotheseClustering.setPas(nouveauPas);
        return nouveauPas;
    }

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

    /**
     * valeur sigmoide inverse custom pour mapper entre valeurs min et max
     * en xMin => valeur retournée = 0.88
     * en xMax => valeur retournée 0.12
     * plafond entre 0.9 et 0.1
     */
    private static class InverseSigmoidFunction {
        private final static float yMin = 0.1f;
        private final static float yMax = 0.9f;
        private final static int alpha = 1;
        private final static int VALEUR_PLATEAU = 1;
        private final double coeffA;
        private final double coeffB;

        public InverseSigmoidFunction(double xMin, double xMax) {
            this.coeffA = (2 + 2) / (xMax - xMin);
            this.coeffB = -2 - (coeffA * xMin);
        }

        public double getValeur(double x) {
            double valeurMappee = coeffA * x + coeffB;
            double valeurY = VALEUR_PLATEAU / (1 + Math.exp(alpha * valeurMappee));
            return Math.min(Math.max(yMin, valeurY), yMax);
        }
    }
}
