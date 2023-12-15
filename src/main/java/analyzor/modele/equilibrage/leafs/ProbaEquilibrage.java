package analyzor.modele.equilibrage.leafs;

import analyzor.modele.equilibrage.ObjetEquilibrage;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * attribue des valeurs
 */
public class ProbaEquilibrage {
    private static final Logger logger = LogManager.getLogger(ProbaEquilibrage.class);
    private final int N_SIMUS_FOLD = 1000;
    // attention il s'agit du nombre de simus / possibilité d'action donc va augmenter quand le pas diminue
    private final int N_SIMUS_ACTION = 200;
    private final int nSituations;
    private final int pas;
    private final int nCategories;

    public ProbaEquilibrage(int nSituations, int pas) {
        this.nSituations = nSituations;
        this.pas = pas;
        nCategories = (100 / this.pas) + 1;
    }

    public void calculerProbas(ObjetEquilibrage comboDenombrable) {
        loggerNomCombo(comboDenombrable);

        calculerProbasActions(comboDenombrable);
        int[] strategiePlusProbableSansFold = comboDenombrable.strategiePlusProbableSansFold();
        if (Arrays.stream(strategiePlusProbableSansFold).sum() != 100)
            throw new RuntimeException("La stratégie sans fold n'est pas égal à 100");
        logger.trace("Stratégie sans fold récupérée depuis ComboDénombrable");
        loggerStrategie(strategiePlusProbableSansFold);
        calculerProbaFold(comboDenombrable, strategiePlusProbableSansFold);
    }

    private void calculerProbasActions(ObjetEquilibrage comboDenombrable) {
        BinomialDistribution distributionCombosServis =
                new BinomialDistribution(nSituations, comboDenombrable.getPCombo());
        float[] pctShowdown = comboDenombrable.getShowdowns();

        for (int i = 0; i < comboDenombrable.getObservations().length; i++) {
            // on regarde tous les % possibles selon pas choisi
            int nombreCategories = nCategories;
            int[] compteCategories = new int[nombreCategories];

            // on compte le nombre de fois où le résultat est égal aux observations
            for (int j = 0; j < nombreCategories; j++) {
                float pctAction = (float) (j * this.pas) / 100;
                // on fixe un seuil mini pour éviter l'effet de seuil dès qu'on a une observation
                if (pctAction == 0) pctAction = (float) (this.pas / 100) / 5;
                compteCategories[j] = echantillonerAction(comboDenombrable.getObservations()[i], pctAction,
                        pctShowdown[i], distributionCombosServis);
            }

            float[] probaDiscretisees = valeursRelatives(compteCategories);

            if (probaDiscretisees == null) probaDiscretisees = probaActionPleine();

            comboDenombrable.setProbaAction(i, probaDiscretisees);
            loggerProbabilites("index " + i, probaDiscretisees);
        }
    }

    /**
     * appelée en cas de bug de l'échantillonage car valeur extrème, l'action est à 100% de proba
     * @return une proba où l'action est sûre
     */
    private float[] probaActionPleine() {
        float[] probaActionPleine = new float[nCategories];
        probaActionPleine[nCategories - 1] = 1 - ((nCategories - 1) * valeurMinimaleProba());
        for (int i = 0; i < probaActionPleine.length - 1; i++) {
            probaActionPleine[i] = valeurMinimaleProba();
        }

        return probaActionPleine;
    }

    /**
     * échantillonne la distribution de probabilité pour une action
     * On a :
     *          observations_estimees = Nservis * (% action) * p(showdown)
     *          Nservis et p(showdown) sont des expériences de Bernoulli
     *          on regarde le nombre de fois où observations_estimées = observations_reelles
     */
    private int echantillonerAction(int observation, float pctAction,
            float pShowdown, BinomialDistribution distributionCombosServis) {
        int observationsConformes = 0;

        for (int i = 0; i < N_SIMUS_ACTION; i++) {
            int nombreServis = getCombosServis(distributionCombosServis);
            int nombreJoues = Math.round((float) (nombreServis * pctAction));
            int nombreObserves = simulerShowdown(nombreJoues, pShowdown);

            if (nombreObserves == observation) observationsConformes++;
        }

        return observationsConformes;
    }

    private void calculerProbaFold(ObjetEquilibrage comboDenombrable, int[] strategieSansFold) {
        if (comboDenombrable.notFolded()) {
            float[] probaNotFold = probaZeroFold();
            loggerProbabilites("FOLD", probaNotFold);
            comboDenombrable.setProbaFold(probaNotFold);
            return;
        }

        // on regarde tous les % possibles selon pas choisi
        int[] compteCategories = new int[nCategories];

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
        if (probaDiscretisees == null) {
            // le seul cas où ça bugue, c'est quand trop d'observations et donc on fold jamais
            probaDiscretisees = probaZeroFold();
        }
        loggerProbabilites("FOLD", probaDiscretisees);
        comboDenombrable.setProbaFold(probaDiscretisees);
    }

    /**
     * au cas où not folded => on rend impossible le changement
     */
    private float[] probaFoldImpossible() {
        float[] probaZeroFold = new float[nCategories];
        probaZeroFold[0] = 1;
        for (int i = 1; i < probaZeroFold.length; i++) {
            probaZeroFold[i] = 0;
        }

        return probaZeroFold;
    }

    /**
     * méthode appelée en cas de bug des calculs de proba => veut forcément dire qu'on ne fold jamais
     * @return une proba où on fold jamais
     */
    private float[] probaZeroFold() {
        float[] probaZeroFold = new float[nCategories];
        probaZeroFold[0] = 1 - ((nCategories - 1) * valeurMinimaleProba());
        for (int i = 1; i < probaZeroFold.length; i++) {
            probaZeroFold[i] = valeurMinimaleProba();
        }

        return probaZeroFold;
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
     * on retourne null si pas de possibilité de calculer la proba
     */
    private float[] valeursRelatives(int[] compteCategories) {
        int totalCompte = 0;
        float[] valeursRelatives = new float[compteCategories.length];

        for (int i = 0; i < compteCategories.length; i++) {
            totalCompte += compteCategories[i];
        }
        // todo comment régler ça?
        if (totalCompte == 0) {
            logger.error("Aucune simulation conforme aux obserations, on remplit avec des probas simplifiées");
            return null;
        }

        for (int i = 0; i < compteCategories.length; i++) {
            valeursRelatives[i] = (float) compteCategories[i] / totalCompte;
            // on ne veut pas de valeur nulle car ça fout la merde dans les multiplications
            if (valeursRelatives[i] == 0) valeursRelatives[i] = valeurMinimaleProba();
        }

        return valeursRelatives;
    }

    private float valeurMinimaleProba() {
        return (float) (100 / nCategories) / 10000;
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
    private void loggerNomCombo(ObjetEquilibrage comboDenombrable) {
        if((!logger.isTraceEnabled())) return;
        // affichage pour suivi des valeurs
        logger.debug("Calcul de probabilités pour : " + comboDenombrable.toString());

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
        if (probaDiscretisees == null) {
            System.out.println("null");
            return;
        }
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
