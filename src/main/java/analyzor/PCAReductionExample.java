package analyzor;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class PCAReductionExample {
    public static void main(String[] args) {
        // Création d'un ensemble de données en grande dimension
        double[][] data = {{1, 2, 3}, {4, 5, 6}, {10, 8, 9}, {58, 11, 12}};
        Array2DRowRealMatrix originalMatrix = new Array2DRowRealMatrix(data);

        // Calcul de la matrice de covariance
        RealMatrix covarianceMatrix = originalMatrix.transpose().multiply(originalMatrix);

        // Décomposition en valeurs singulières (SVD)
        SingularValueDecomposition svd = new SingularValueDecomposition(covarianceMatrix);

        // Récupération des valeurs singulières
        double[] singularValues = svd.getSingularValues();

        // Calcul de la variance cumulée
        double totalVariance = 0.0;
        double[] cumulativeVariance = new double[singularValues.length];
        for (int i = 0; i < singularValues.length; i++) {
            totalVariance += singularValues[i];
            cumulativeVariance[i] = totalVariance;
        }

        // Normalisation de la variance cumulée
        for (int i = 0; i < cumulativeVariance.length; i++) {
            cumulativeVariance[i] /= totalVariance;
        }

        // Affichage de la variance cumulée
        System.out.println("Variance cumulée des composantes principales :");
        for (int i = 0; i < cumulativeVariance.length; i++) {
            System.out.println("Dimension " + (i + 1) + ": " + cumulativeVariance[i]);
        }
    }
}
