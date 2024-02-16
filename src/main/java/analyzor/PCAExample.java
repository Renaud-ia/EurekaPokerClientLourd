package analyzor;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

import java.util.Arrays;

public class PCAExample {

    public static void main(String[] args) {
        // Exemple de données (4 observations, 3 variables)
        double[][] data = {
                {25, 18, 3},
                {6, 2, 19},
                {22, 32, 4},
                {2, 1, 21}
        };

        // Création d'une matrice de données
        RealMatrix matrix = new Array2DRowRealMatrix(data);

        // Calcul de la matrice de covariance
        Covariance covariance = new Covariance(matrix);
        RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();

        // Décomposition en valeurs propres
        EigenDecomposition eigenDecomposition = new EigenDecomposition(covarianceMatrix);

        // Obtention des valeurs propres et vecteurs propres
        double[] eigenvalues = eigenDecomposition.getRealEigenvalues();
        RealMatrix eigenvectors = eigenDecomposition.getV();

        // Calcul de la somme totale des valeurs propres
        double totalEigenvalues = 0.0;
        for (double eigenvalue : eigenvalues) {
            totalEigenvalues += eigenvalue;
        }

        // Calcul du pourcentage de variance expliquée par chaque composante principale
        System.out.println("Pourcentage de variance expliquée par chaque composante : ");
        for (int i = 0; i < eigenvalues.length; i++) {
            double varianceExplained = (eigenvalues[i] / totalEigenvalues) * 100;
            System.out.println("Composante " + (i + 1) + ": " + varianceExplained + "%");
        }

        // Affichage des vecteurs propres
        System.out.println("Vecteurs propres : ");
        for (int i = 0; i < eigenvectors.getColumnDimension(); i++) {
            System.out.println("Composante " + (i + 1) + ": " + eigenvectors.getColumnVector(i));
        }

        // Projection des données originales sur les axes de l'ACP
        RealMatrix transformedData = matrix.multiply(eigenvectors);
        System.out.println("Données transformées : ");
        for (double[] pointSortie : transformedData.getData()) {
            System.out.println(Arrays.toString(pointSortie));
        }
    }
}