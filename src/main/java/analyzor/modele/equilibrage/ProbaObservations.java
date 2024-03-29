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


public class ProbaObservations implements Runnable {
    
    private final static int N_ECHANTILLONS = 1000;
    
    private final static int N_SIMUS_ACTION = 50;
    private final static int N_SIMUS_SHOWDOWN = 50;
    
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


    
    @Override
    public void run() {
        if (Estimateur.estInterrompu()) {
            throw new RuntimeException("Interruption");
        }

        
        LinkedList<BetaDistribution> distributions = genererDistributionsBeta();
        float[][] probaDiscretisees = echantillonnerProbas(distributions);
        noeudEquilibrage.setProbabilitesObservations(probaDiscretisees);
    }


    

    
    private LinkedList<BetaDistribution> genererDistributionsBeta() {
        LinkedList<BetaDistribution> betaGenerees = new LinkedList<>();

        for (int i = 0; i < noeudEquilibrage.getObservations().length; i++) {
            
            final int N_TENTATIVES = 10;
            for (int j = 0; j < N_TENTATIVES; j++) {
                try {
                    BetaDistribution betaCalculee = trouverMeilleureBeta(i);
                    betaGenerees.add(betaCalculee);
                    break;
                }
                catch (Exception ignored) {
                    if (j == N_TENTATIVES - 1) throw new RuntimeException(N_TENTATIVES + "erreurs d'optimisation");
                }
            }
        }


        return betaGenerees;
    }

    private BetaDistribution trouverMeilleureBeta(int indexAction) {
        
        MultivariateFunction objectiveFunction = point -> {
            double alpha = point[0];
            double beta = point[1];

            
            return calculerErreur(alpha, beta, indexAction);
        };

        
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        
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

    
    private float fonctionCout(double ValeurPredite, double valeurObservee) {
        return (float) Math.abs(Math.log(ValeurPredite + 1) - Math.log(valeurObservee + 1));
    }


    

    
    private float[][] echantillonnerProbas(LinkedList<BetaDistribution> distributions) {
        int nActions = distributions.size();
        if (foldPossible) nActions++;

        
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

    
    private float[][] discretiserProbas(float[][] echantillonnage) {
        float[][] probaDiscretisees = new float[echantillonnage.length][];

        for (int i = 0; i < echantillonnage.length; i++) {
            probaDiscretisees[i] = valeursRelatives(echantillonnage[i]);
        }

        return probaDiscretisees;
    }

    
    private float[] valeursRelatives(float[] compteCategories) {
        float pasDiscretisation = (float) pas / 100;
        int nCategories = (int) Math.ceil((double) 100 / pas) + 1;

        
        int[] counts = new int[nCategories];

        
        for (float valeur : compteCategories) {
            for (int i = 1; i < counts.length - 1; i++) {
                float borneInferieure;
                float borneSuperieure;

                borneInferieure = (float) ((i - 0.5) * pasDiscretisation);
                borneSuperieure = (float) ((i + 0.5) * pasDiscretisation);

                if (valeur >= borneInferieure && valeur < borneSuperieure) {
                    counts[i]++;
                    break; 
                }
            }
        }
        
        counts[0] = counts[1];
        counts[counts.length - 1] = counts[counts.length - 2];

        
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


    

    public static void setPas(int nouveauPas) {
        if (100 % nouveauPas != 0)
            throw new IllegalArgumentException("Le pas n'est pas un diviseur de 100 : " + nouveauPas);
        pas = nouveauPas;
    }


    public static void setNombreSituations(int nombreSituations) {
        nSituations = nombreSituations;
    }

    
    public static void setFoldPossible(boolean possible) {
        foldPossible = possible;
    }
}

