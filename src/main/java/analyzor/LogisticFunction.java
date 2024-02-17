public class LogisticFunction {
    private final double xMin;
    private final double xMax;

    public LogisticFunction(double xMin, double xMax) {
        this.xMin = xMin;
        this.xMax = xMax;
    }

    public double calculate(double x) {
        double k = -0.03; // Coefficient de croissance
        double x0 = 150; // Valeur de x à laquelle la fonction atteint la moitié de sa valeur maximale
        double L = 1; // Valeur maximale de la fonction

        return 1 / (1 + Math.exp(-k * (x - x0)));
    }

    public static void main(String[] args) {
        double xMin = 0; // Valeur minimale de x
        double xMax = 500; // Valeur maximale de x

        LogisticFunction logisticFunction = new LogisticFunction(xMin, xMax);

        // Exemple : Calcul de la valeur de la fonction pour x = 100
        double result = logisticFunction.calculate(100);
        System.out.println("Résultat : " + result);
    }
}