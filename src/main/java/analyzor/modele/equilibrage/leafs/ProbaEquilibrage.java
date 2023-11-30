package analyzor.modele.equilibrage.leafs;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * attribue des valeurs
 */
public class ProbaEquilibrage {
    private static final Logger logger = LogManager.getLogger(ProbaEquilibrage.class);
    private final int N_SIMUS_FOLD = 10000;
    // attention il s'agit du nombre de simus / possibilité d'action donc va augmenter quand le pas diminue
    private final int N_SIMUS_ACTION = 1000;
    private final int nSituations;
    private final int pas;

    public ProbaEquilibrage(int nSituations, int pas) {
        this.nSituations = nSituations;
        this.pas = pas;
    }

    public void calculerProbas(ComboDenombrable comboDenombrable) {
        loggerNomCombo(comboDenombrable);

        calculerProbasActions(comboDenombrable);
        int[] strategiePlusProbableSansFold = comboDenombrable.strategiePlusProbableSansFold();
        if (Arrays.stream(strategiePlusProbableSansFold).sum() != 100)
            throw new RuntimeException("La stratégie sans fold n'est pas égal à 100");
        logger.trace("Stratégie sans fold récupérée depuis ComboDénombrable");
        loggerStrategie(strategiePlusProbableSansFold);
        calculerProbaFold(comboDenombrable, strategiePlusProbableSansFold);
    }

    private void calculerProbasActions(ComboDenombrable comboDenombrable) {
        BinomialDistribution distributionCombosServis =
                new BinomialDistribution(nSituations, comboDenombrable.getPCombo());
        float[] pctShowdown = comboDenombrable.getShowdowns();

        for (int i = 0; i < comboDenombrable.getObservations().length; i++) {
            // on regarde tous les % possibles selon pas choisi
            int nombreCategories = (100 / this.pas) + 1;
            int[] compteCategories = new int[nombreCategories];

            // on compte le nombre de fois où le résultat est égal aux observations
            for (int j = 0; j < nombreCategories; j++) {
                int pctAction = j * this.pas;
                compteCategories[j] = echantillonerAction(comboDenombrable.getObservations()[i], pctAction,
                        pctShowdown[i], distributionCombosServis);
            }

            float[] probaDiscretisees = valeursRelatives(compteCategories);

            comboDenombrable.setProbaAction(i, probaDiscretisees);
            loggerProbabilites("index " + i, probaDiscretisees);
        }
    }

    /**
     * échantillonne la distribution de probabilité pour une action
     * On a :
     *          observations_estimees = Nservis * (% action) * p(showdown)
     *          Nservis et p(showdown) sont des expériences de Bernoulli
     *          on regarde le nombre de fois où observations_estimées = observations_reelles
     */
    private int echantillonerAction(int observation, int pctAction,
            float pShowdown, BinomialDistribution distributionCombosServis) {
        int observationsConformes = 0;

        for (int i = 0; i < N_SIMUS_ACTION; i++) {
            int nombreServis = getCombosServis(distributionCombosServis);
            int nombreJoues = Math.round((float) (nombreServis * pctAction) / 100);
            int nombreObserves = simulerShowdown(nombreJoues, pShowdown);

            if (nombreObserves == observation) observationsConformes++;
        }

        return observationsConformes;
    }

    private void calculerProbaFold(ComboDenombrable comboDenombrable, int[] strategieSansFold) {
        // on regarde tous les % possibles selon pas choisi
        int nombreCategories = (100 / this.pas) + 1;
        int[] compteCategories = new int[nombreCategories];

        BinomialDistribution distributionCombosServis =
                new BinomialDistribution(nSituations, comboDenombrable.getPCombo());
        float[] pctShowdown = comboDenombrable.getShowdowns();
        int observations = Arrays.stream(comboDenombrable.getObservations()).sum();

        for (int i = 0; i < compteCategories.length; i++) {
            int pctFold = i * this.pas;
            compteCategories[i] = echantillonnerFold(distributionCombosServis, strategieSansFold,
                    pctShowdown, observations, pctFold);
        }

        float[] probaDiscretisees = valeursRelatives(compteCategories);
        loggerProbabilites("FOLD", probaDiscretisees);
        comboDenombrable.setProbaFold(probaDiscretisees);
    }

    /**
     * calcule un échantillon de probabilité de fold
     * On a :
     *          nombre_combos_showdown =  ∑ (pctAction * nJoues * p(showdown) )
     *          avec Njoues = Nservis * (1 - p(fold))
     *          on compte le nombre de fois où combos_observes = nombre_combos_showdown
     */
    private int echantillonnerFold(BinomialDistribution distributionCombosServis, int[] strategieSansFold,
                                   float[] pctShowdown, int nombreObservations, int pctFold) {
        int nombreServis = Math.round((float) (getCombosServis(distributionCombosServis) * (100 - pctFold)) / 100);

        if (strategieSansFold.length != pctShowdown.length)
            throw new IllegalArgumentException("Pas autant d'actions que de showdown");

        int compteExacte = 0;
        for (int i = 0; i < N_SIMUS_FOLD; i++) {
            int totalObserves = 0;
            for (int j = 0; j < strategieSansFold.length; j++) {
                float pctAction = (float) strategieSansFold[j] / 100;
                int nombreJoues = Math.round(pctAction * nombreServis);
                totalObserves += simulerShowdown(nombreJoues, pctShowdown[j]);
            }
            if (totalObserves == nombreObservations) compteExacte++;
        }

        return compteExacte;
    }

    private int getCombosServis(BinomialDistribution distributionCombosServis) {
        return distributionCombosServis.sample();
    }

    /**
     * le showdown suit une loi de bernoulli => tend vers loi normale avec grand nombre d'échantillons
     */
    private int simulerShowdown(int nombreJoues, float pShowdown) {
        if (nombreJoues == 0) return 0;
        BinomialDistribution distributionShowdown = new BinomialDistribution(nombreJoues, pShowdown);
        return distributionShowdown.sample();
    }

    /**
     * on va juste calculer la distribution des comptes
     */
    private float[] valeursRelatives(int[] compteCategories) {
        int totalCompte = 0;
        float[] valeursRelatives = new float[compteCategories.length];

        for (int i = 0; i < compteCategories.length; i++) {
            totalCompte += compteCategories[i];
        }
        for (int i = 0; i < compteCategories.length; i++) {
            valeursRelatives[i] = (float) compteCategories[i] / totalCompte;
        }

        return valeursRelatives;
    }

    @Deprecated
    public static int getBinomial(int n, double p) {
        double log_q = Math.log(1.0 - p);
        int x = 0;
        double sum = 0;
        for(;;) {
            sum += Math.log(Math.random()) / (n - x);
            if(sum < log_q) {
                return x;
            }
            x++;
        }
    }

    // todo : pour débug à supprimer ?
    private void loggerNomCombo(ComboDenombrable comboDenombrable) {
        if((!logger.isTraceEnabled())) return;
        // affichage pour suivi des valeurs
        logger.trace("Calcul de probabilités pour : " + comboDenombrable.toString());

        StringBuilder observations = new StringBuilder();
        observations.append("Observations : [");
        for (int observation : comboDenombrable.getObservations()) {
            observations.append(observation);
            observations.append(", ");
        }
        observations.append("]");
        logger.trace(observations.toString());

        StringBuilder showdowns = new StringBuilder();
        showdowns.append("Showdowns : [");
        for (float show : comboDenombrable.getShowdowns()) {
            showdowns.append(show);
            showdowns.append(", ");
        }
        showdowns.append("]");
        logger.trace(showdowns.toString());
    }

    // todo suivi valeurs à supprimer
    private void loggerProbabilites(String refAction, float[] probaDiscretisees) {
        if((!logger.isTraceEnabled())) return;
        // affichage pour suivi valeur
        StringBuilder probaString = new StringBuilder();
        probaString.append("PROBABILITE pour action ").append(refAction);
        probaString.append(": [");
        for (float probaDiscretisee : probaDiscretisees) {
            probaString.append(probaDiscretisee).append(", ");
        }
        probaString.append("]");
        logger.trace(probaString.toString());
    }

    // todo suivi valeurs à supprimer
    private void loggerStrategie(int[] strategiePlusProbableSansFold) {
        if((!logger.isTraceEnabled())) return;
        StringBuilder strategieString = new StringBuilder();
        strategieString.append("stratégie : [");
        for (int valeur : strategiePlusProbableSansFold) {
            strategieString.append(valeur).append(", ");
        }
        strategieString.append("]");
        logger.trace(strategieString.toString());
    }
}
