package analyzor;

import org.apache.commons.math3.special.Gamma;

public class DirichletDensity {
    public static double dirichletDensity(double[] x, double[] alpha) {
        if (x.length != alpha.length) {
            throw new IllegalArgumentException("Dimensions of x and alpha must be equal");
        }

        // Calculer la somme des paramètres alpha
        double sumAlpha = 0;
        for (double a : alpha) {
            sumAlpha += a;
        }

        // Calculer le coefficient de normalisation
        double coef = Gamma.gamma(sumAlpha);
        for (int i = 0; i < alpha.length; i++) {
            coef /= Gamma.gamma(alpha[i]);
        }

        // Calculer la densité de probabilité
        double product = 1;
        for (int i = 0; i < x.length; i++) {
            product *= Math.pow(x[i], alpha[i] - 1);
        }

        return coef * product;
    }

    public static void main(String[] args) {
        double[] x = {0.3, 0.4, 0.3}; // Variables aléatoires
        double[] alpha = {2.0, 3.0, 4.0}; // Paramètres alpha

        double density = dirichletDensity(x, alpha);
        System.out.println("Density: " + density);
    }
}