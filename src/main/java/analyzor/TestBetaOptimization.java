package analyzor;

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

public class TestBetaOptimization {
        public static void main(String[] args) {
            // Fonction objectif pour minimiser l'erreur absolue moyenne
            MultivariateFunction objectiveFunction = point -> {
                double alpha = point[0];
                double beta = point[1];

                // Calcul de l'erreur absolue moyenne pour les paramètres alpha et beta donnés
                double error = calculateError(alpha, beta);

                return error;
            };

            // Optimisation bayésienne
            BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
            double[] initialGuess = {1, 1}; // Valeurs initiales de alpha et beta
            PointValuePair result = optimizer.optimize(
                    new MaxEval(1000),
                    new ObjectiveFunction(objectiveFunction),
                    GoalType.MINIMIZE,
                    new InitialGuess(initialGuess),
                    new SimpleBounds(new double[]{1, 1}, new double[]{30, 30}));

            double alphaOptimal = result.getPoint()[0];
            double betaOptimal = result.getPoint()[1];
            double minError = result.getValue();

            System.out.println("Paramètres optimaux :");
            System.out.println("Alpha : " + alphaOptimal);
            System.out.println("Beta : " + betaOptimal);
            System.out.println("Erreur absolue moyenne minimale : " + minError);
        }

        // Fonction pour calculer l'erreur absolue moyenne
        private static float calculateError(double alpha, double beta) {
            final float pShowdownEstime = 0.13284285f;
            final int observations = 3;

            BetaDistribution betaDistribution = new BetaDistribution(alpha, beta);

            BinomialDistribution nombreCombosServis = new BinomialDistribution(8216, 0.057315234);

            final int nSimusProbaAction = 50;
            final int nSimusShowdown = 50;
            float valeurErreur = 0;
            for (int i = 0; i < nSimusProbaAction; i++) {
                // Générer un échantillon à partir de la distribution bêta
                double probaAction = betaDistribution.sample();
                int nCombosServis = nombreCombosServis.sample();
                int nCombosJoues = (int) (probaAction * nCombosServis);

                for (int j = 0; j < nSimusShowdown; j++) {
                    BinomialDistribution pShowdown = new BinomialDistribution(nCombosJoues, pShowdownEstime);
                    int nCombosShowdown = pShowdown.sample();
                    valeurErreur += (float) Math.abs(Math.log(nCombosShowdown + 1) - Math.log(observations + 1));
                }

            }

            return valeurErreur / (nSimusShowdown * nSimusProbaAction);
        }
    }
