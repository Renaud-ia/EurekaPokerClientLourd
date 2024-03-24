package analyzor.modele.equilibrage;

import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import analyzor.modele.estimation.CalculInterrompu;
import analyzor.modele.estimation.Estimateur;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * attribue des probabilités à chaque action selon les observations
 *
 */
public class ProbaObservations implements Runnable {
    private static final Logger logger = LogManager.getLogger(ProbaObservations.class);
    // todo OPTIMISATION : trouver les bonnes valeurs
    // valeurs de config pour échantillonnage
    private final static int N_ECHANTILLONS = 1000;
    // valeurs de config pour calcul de l'erreur
    private final static int N_SIMUS_ACTION = 50;
    private final static int N_SIMUS_SHOWDOWN = 50;
    // valeurs de config pour l'optimisation bayésienne
    private static final int MAX_EVAL = 1000;
    private static final int MIN_ALPHA_BETA = 1;
    private static final int MAX_ALPHA_BETA = 30;
    private static final int INITIALISATION_ALPHA_BETA = 1;
    private static int nSituations;
    private static int pas;
    private static boolean foldPossible;
    private final NoeudEquilibrage noeudEquilibrage;

    public ProbaObservations(NoeudEquilibrage noeudEquilibrage) {
        this.noeudEquilibrage = noeudEquilibrage;
    }


    // interface publique pour multiprocesser
    @Override
    public void run() {
        if (Estimateur.estInterrompu()) {
            throw new RuntimeException();
        }



        // d'abord on trouve les distributions bêta
        LinkedList<BetaDistribution> distributions = genererDistributionsBeta();
        float[][] probaDiscretisees = echantillonnerProbas(distributions);
        logger.trace("Proba calculées pour : " + noeudEquilibrage + "\n"
                + ", observations : " + Arrays.toString(noeudEquilibrage.getObservations()) + "\n"
                + "probas : " + Arrays.deepToString(probaDiscretisees));
        noeudEquilibrage.setProbabilitesObservations(probaDiscretisees);
    }


    // méthodes privées de calcul des bêta

    /**
     * génère un ensemble de distributions bêta pour chaque action observable (=pas fold)
     * @return une liste dans l'ordre des actions
     */
    private LinkedList<BetaDistribution> genererDistributionsBeta() {
        LinkedList<BetaDistribution> betaGenerees = new LinkedList<>();

        for (int i = 0; i < noeudEquilibrage.getObservations().length; i++) {
            // vu que ça bug parfois on retente plusieurs fois
            final int N_TENTATIVES = 10;
            for (int j = 0; j < N_TENTATIVES; j++) {
                try {
                    BetaDistribution betaCalculee = trouverMeilleureBeta(i);
                    betaGenerees.add(betaCalculee);
                    break;
                }
                catch (Exception e) {
                    logger.debug("Erreur dans l'optimisation de : " + noeudEquilibrage + ", index action : " + i);
                    logger.debug("PCombo : " + noeudEquilibrage.getPCombo());
                    logger.debug("Observations : " + Arrays.toString(noeudEquilibrage.getObservations()));
                    logger.debug("Showdowns : " + Arrays.toString(noeudEquilibrage.getShowdowns()));
                    logger.debug("Le calcul n'a pas été fait pour : " + noeudEquilibrage, e);
                }
            }
        }


        return betaGenerees;
    }

    private BetaDistribution trouverMeilleureBeta(int indexAction) {
        // Fonction objectif pour minimiser l'erreur absolue moyenne
        MultivariateFunction objectiveFunction = point -> {
            double alpha = point[0];
            double beta = point[1];

            // Calcul de l'erreur absolue moyenne pour les paramètres alpha et beta donnés
            return calculerErreur(alpha, beta, indexAction);
        };

        // Optimisation bayésienne
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        // Valeurs initiales de alpha et beta
        double[] initialGuess = {INITIALISATION_ALPHA_BETA, INITIALISATION_ALPHA_BETA};
        PointValuePair result = optimizer.optimize(
                new MaxEval(MAX_EVAL),
                new ObjectiveFunction(objectiveFunction),
                GoalType.MINIMIZE,
                new InitialGuess(initialGuess),
                new SimpleBounds(new double[]{MIN_ALPHA_BETA, MIN_ALPHA_BETA},
                        new double[]{MAX_ALPHA_BETA, MAX_ALPHA_BETA}));

        double alphaOptimal = result.getPoint()[0];
        double betaOptimal = result.getPoint()[1];

        return new BetaDistribution(alphaOptimal, betaOptimal);
    }

    private double calculerErreur(double alpha, double beta, int indexAction) {
        final float pShowdownEstime = noeudEquilibrage.getShowdowns()[indexAction];
        final int observations = noeudEquilibrage.getObservations()[indexAction];

        BetaDistribution betaDistribution = new BetaDistribution(alpha, beta);
        BinomialDistribution nombreCombosServis = new BinomialDistribution(nSituations, noeudEquilibrage.getPCombo());

        final int nSimusProbaAction = N_SIMUS_ACTION;
        final int nSimusShowdown = N_SIMUS_SHOWDOWN;
        float valeurErreur = 0;
        for (int i = 0; i < nSimusProbaAction; i++) {
            // Générer un échantillon à partir de la distribution bêta
            double probaAction = betaDistribution.sample();
            int nCombosServis = nombreCombosServis.sample();
            int nCombosJoues = (int) (probaAction * nCombosServis);

            for (int j = 0; j < nSimusShowdown; j++) {
                BinomialDistribution pShowdown = new BinomialDistribution(nCombosJoues, pShowdownEstime);
                int nCombosShowdown = pShowdown.sample();
                valeurErreur += fonctionCout(nCombosShowdown, observations);
            }

        }

        return valeurErreur / (nSimusShowdown * nSimusProbaAction);
    }

    /**
     * type de fonction cout
     * @return la valeur de l'erreur
     */
    private float fonctionCout(double ValeurPredite, double valeurObservee) {
        return (float) Math.abs(Math.log(ValeurPredite + 1) - Math.log(valeurObservee + 1));
    }


    // méthodes privées d'échantillonnage des probas

    /**
     * vérifie que fold est possible ou non
     * @param distributions la liste des distributions bêta pour chaque action
     * @return les probas pour chaque action
     */
    private float[][] echantillonnerProbas(LinkedList<BetaDistribution> distributions) {
        int nActions = distributions.size();
        if (foldPossible) nActions++;

        // on va échantillonner à partir des bêta
        float[][] echantillonnage = new float[nActions][N_ECHANTILLONS];

        for (int i = 0; i < N_ECHANTILLONS; i++) {
            int indexAction = 0;
            float sumAction = 0;
            for (BetaDistribution betaDistribution : distributions) {
                float pctAction = (float) betaDistribution.sample();
                echantillonnage[indexAction++][i] = pctAction;
                sumAction += pctAction;
            }

            if (foldPossible) {
                echantillonnage[indexAction][i] = 1 - sumAction;
            }
        }

        return discretiserProbas(echantillonnage);
    }

    /**
     * discrétise les probas échantillonnées selon le pas choisi
     * @param echantillonnage valeur des échantillons par action
     * @return
     */
    private float[][] discretiserProbas(float[][] echantillonnage) {
        float[][] probaDiscretisees = new float[echantillonnage.length][];

        for (int i = 0; i < echantillonnage.length; i++) {
            probaDiscretisees[i] = valeursRelatives(echantillonnage[i]);
        }

        return probaDiscretisees;
    }

    /**
     * on va juste calculer la distribution des comptes
     * si une proba est <= 0, on remplace par une valeur minimale
     */
    private float[] valeursRelatives(float[] compteCategories) {
        float pasDiscretisation = (float) pas / 100;
        int nCategories = (int) Math.ceil((double) 100 / pas) + 1;

        // Initialisation des compteurs pour chaque intervalle
        int[] counts = new int[nCategories];

        // Compter les valeurs dans chaque intervalle
        for (float valeur : compteCategories) {
            for (int i = 1; i < counts.length - 1; i++) {
                float borneInferieure;
                float borneSuperieure;

                borneInferieure = (float) ((i - 0.5) * pasDiscretisation);
                borneSuperieure = (float) ((i + 0.5) * pasDiscretisation);

                if (valeur >= borneInferieure && valeur < borneSuperieure) {
                    counts[i]++;
                    break; // Sortir de la boucle une fois que la valeur est comptée
                }
            }
        }
        // pour éviter l'effet de seuil, on reproduit juste les valeurs de la proba d'à côté pour première et dernière
        counts[0] = counts[1];
        counts[counts.length - 1] = counts[counts.length - 2];

        // Calculer le pourcentage de valeurs dans chaque intervalle
        int totalCount = compteCategories.length;
        float[] percentages = new float[counts.length];
        for (int i = 0; i < counts.length; i++) {
            float pourcentage = (float) counts[i] / totalCount;
            if (pourcentage == 0) {
                pourcentage = valeurMinimaleProba();
            }
            percentages[i] = pourcentage;
        }

        return percentages;
    }

    private float valeurMinimaleProba() {
        return (float) pas / 10000;
    }


    // méthodes statiques pour modifier les attributs statiques

    public static void setPas(int nouveauPas) {
        if (100 % nouveauPas != 0)
            throw new IllegalArgumentException("Le pas n'est pas un diviseur de 100 : " + nouveauPas);
        pas = nouveauPas;
    }


    public static void setNombreSituations(int nombreSituations) {
        nSituations = nombreSituations;
    }

    /**
     * important car on veut ne pas construire une proba qui n'existe pas
     * @param possible est ce que le fold fait partie des actions possibles
     */
    public static void setFoldPossible(boolean possible) {
        foldPossible = possible;
    }
}

