package analyzor;

/**
 * valeur sigmoide inverse custom
 * en xMin => valeur retournée = 0.88
 * en xMax => valeur retournée 0.12
 * plafond entre 0.9 et 0.1
 */
public class InverseSigmoidFunction {
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

    public double calculate(double x) {
        double valeurMappee = coeffA * x + coeffB;
        double valeurY = VALEUR_PLATEAU / (1 + Math.exp(-alpha * valeurMappee));
        return Math.min(Math.max(yMin, valeurY), yMax);
    }

    public static void main(String[] args) {
        double xMin = 50; // Valeur minimale de x
        double xMax = 1000; // Valeur maximale de x

        InverseSigmoidFunction inverseSigmoidFunction = new InverseSigmoidFunction(xMin, xMax);

        // Exemple : Calcul de la valeur de la fonction pour x = 100
        double result = inverseSigmoidFunction.calculate(50);
        System.out.println("Résultat : " + result);
    }
}
