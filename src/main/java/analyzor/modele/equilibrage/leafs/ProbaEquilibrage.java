package analyzor.modele.equilibrage.leafs;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * attribue des valeurs
 */
public class ProbaEquilibrage {
    private final int N_SIMUS_FOLD = 1000;
    private final int N_SIMUS_ACTION = 1000;
    private final int nSituations;
    private final int pas;

    public ProbaEquilibrage(int nSituations, int pas) {
        // todo on pourrait faire varier les nombres de simus selon nSituation et step
        this.nSituations = nSituations;
        this.pas = pas;
    }

    public void calculerProbas(ComboDenombrable comboDenombrable) {
        calculerProbaFold(comboDenombrable);
        calculerProbasActions(comboDenombrable);
    }

    private void calculerProbasActions(ComboDenombrable comboDenombrable) {
        BinomialDistribution distributionCombosServis =
                new BinomialDistribution(nSituations, comboDenombrable.getPCombo());
        NormalDistribution[] distributionShowdown = getShowdownDistribution(comboDenombrable);

        for (int i = 0; i < comboDenombrable.getObservations().length; i++) {
            float[] probaDiscretisees =
                    echantillonerAction(comboDenombrable.getObservations()[i],
                            distributionShowdown[i], distributionCombosServis);
            comboDenombrable.setProbaAction(i, probaDiscretisees);
        }
    }

    /**
     * échantillonne la distribution de probabilité pour une action
     * On a :
     *          p(action) = nombre_combos_joues / nombre_combos_servis
     *          p(action) = (observés / p(show)) / nombre_combos_servis
     */
    private float[] echantillonerAction(int observation,
            NormalDistribution distributionShowdown, BinomialDistribution distributionCombosServis) {

        List<Float> echantillonsAction = new ArrayList<>();
        for (int i = 0; i < N_SIMUS_ACTION; i++) {
            int nombreServis = getCombosServis(distributionCombosServis);
            float pShowdown = getPShowdown(distributionShowdown);
            float pAction = ((observation / pShowdown) / nombreServis);
            echantillonsAction.add(pAction);
        }

        return discretiserValeurs(echantillonsAction);
    }

    private void calculerProbaFold(ComboDenombrable comboDenombrable) {
        List<Float> valeursFoldEchantillonnees = new ArrayList<>();

        BinomialDistribution distributionCombosServis =
                new BinomialDistribution(nSituations, comboDenombrable.getPCombo());
        NormalDistribution[] distributionShowdown = getShowdownDistribution(comboDenombrable);

        for (int i = 0; i < N_SIMUS_FOLD; i++) {
            Float valeur =
                    echantillonFold(distributionCombosServis, distributionShowdown, comboDenombrable.getObservations());
            valeursFoldEchantillonnees.add(valeur);
        }

        float[] probaDiscretisees = discretiserValeurs(valeursFoldEchantillonnees);
        comboDenombrable.setProbaFold(probaDiscretisees);
    }

    /**
     * calcule un échantillon de probabilité de fold
     * On a :
     *          p(f) = (nombre_combos_servis - nombre_combos_joués) / nombre_combos_servis
     *          p(f) = (nombre_combos_servis - [ ∑(i) (observe(i) / pShowdown(i) ]) / nombre_combos_servis
     */
    private float echantillonFold(BinomialDistribution distributionCombosServis,
                                  NormalDistribution[] showdownAction, int[] observations) {
        int nombreServis = getCombosServis(distributionCombosServis);

        if (observations.length != showdownAction.length)
            throw new IllegalArgumentException("Pas autant de showdown que d'observation");

        int nCombosJoues = 0;
        for (int i = 0; i < showdownAction.length; i++) {
            float pShowdown = getPShowdown(showdownAction[i]);
            nCombosJoues += (int) (observations[i] / pShowdown);
        }

        float pFold = (float) (nombreServis - nCombosJoues) / nombreServis;
        if (pFold < 0) pFold = 0;
        if (pFold > 1) pFold = 1;

        return pFold;

    }

    private NormalDistribution[] getShowdownDistribution(ComboDenombrable comboDenombrable) {
        // todo : deviation standard devrait être fonction de la taille de l'échantillon
        float deviationStandard = 0.3f;
        NormalDistribution[] distributionShowdown = new NormalDistribution[comboDenombrable.getShowdowns().length];
        for (int i = 0; i < comboDenombrable.getShowdowns().length; i++) {
            float valeurShowdown = comboDenombrable.getShowdowns()[i];
            distributionShowdown[i] = new NormalDistribution(valeurShowdown, deviationStandard);
        }
        return distributionShowdown;
    }

    private float[] discretiserValeurs(List<Float> echantillonsAction) {
        int nombreCategories = 100 / this.pas;
        float[] valeursDiscretisees = new float[nombreCategories];

        Collections.sort(echantillonsAction);
        int seuilCherche = 0;
        int indexRecherche = 0;
        for (int i = 0; i < nombreCategories; i++){
            if (i < nombreCategories - 1) seuilCherche += pas;
            else seuilCherche = 101;
            int compte = 0;
            while (indexRecherche < echantillonsAction.size()) {
                if (echantillonsAction.get(indexRecherche) > seuilCherche) break;
                indexRecherche++;
                compte++;
            }
            valeursDiscretisees[i] = compte;
        }

        return valeursDiscretisees;
    }

    private int getCombosServis(BinomialDistribution distributionCombosServis) {
        int nombreServis = distributionCombosServis.sample();
        if (nombreServis == 0) nombreServis = 1;
        return nombreServis;
    }

    private float getPShowdown(NormalDistribution distributionShowdown) {
        float pShowdown = (float) distributionShowdown.sample();
        if (pShowdown == 0) pShowdown = 0.01f;
        return pShowdown;
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
}
