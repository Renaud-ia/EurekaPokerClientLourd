package analyzor;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

public class TestBeta {
    public static void main(String[] args) {
        double alpha = 3; // Paramètre alpha de la distribution bêta
        double beta = 10; // Paramètre beta de la distribution bêta

        final float pShowdownEstime = 0.23700517f;
        final int observations = 0;

        BetaDistribution betaDistribution = new BetaDistribution(alpha, beta);

        BinomialDistribution nombreCombosServis = new BinomialDistribution(500, 0.009049774);

        long startTime = System.currentTimeMillis();
        final int nSimusProbaAction = 50;
        final int nSimusShowdown = 50;
        float valeurErreur = 0;
        for (int i = 0; i < nSimusProbaAction; i++) {
            // Générer un échantillon à partir de la distribution bêta
            double probaAction = betaDistribution.sample();
            int nCombosServis = nombreCombosServis.sample();
            int nCombosJoues = (int) (probaAction * nCombosServis);

            System.out.println("Proba action :" + probaAction);
            System.out.println("Nombre combos servis : " + nCombosServis);
            System.out.println("Nombre combos joués : " + nCombosJoues);

            for (int j = 0; j < nSimusShowdown; j++) {
                BinomialDistribution pShowdown = new BinomialDistribution(nCombosJoues, pShowdownEstime);
                int nCombosShowdown = pShowdown.sample();
                System.out.println("Nombre cmbos showdown : " + nCombosShowdown);
                valeurErreur += Math.abs(nCombosShowdown - observations);
            }

        }

        System.out.println("Erreur absolue moyenne : " + valeurErreur / (nSimusShowdown * nSimusProbaAction));

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("Temps écoulé : " + elapsedTime + " millisecondes");

        double lowerBound = 0.00; // Limite inférieure (10%)
        double upperBound = 0.10; // Limite supérieure (20%)

        // Calcul de la probabilité que p soit entre 10% et 20%
        double probability = betaDistribution.cumulativeProbability(upperBound) - betaDistribution.cumulativeProbability(lowerBound);

        System.out.println("Probabilité que p soit entre 10% et 20% : " + probability);
    }
}
