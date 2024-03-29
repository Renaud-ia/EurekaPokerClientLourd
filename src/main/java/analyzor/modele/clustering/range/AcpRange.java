package analyzor.modele.clustering.range;

import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.clustering.objets.MinMaxCalcul;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.denombrement.combos.DenombrableIso;
import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class AcpRange {

    
    private final static float MIN_PCT_VARIANCE_AXE = 0.10f;
    private final static int MAX_DIMENSIONS = 5;
    private LinkedList<ComboPreClustering> donnees;
    public AcpRange() {
    }

    
    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        donnees = formaterDonnees(noeuds);
    }

    
    private LinkedList<ComboPreClustering> formaterDonnees(List<NoeudEquilibrage> noeuds) {
        
        LinkedList<ComboPreClustering> comboPreClusterings = new LinkedList<>();
        for (NoeudEquilibrage noeudEquilibrage : noeuds) {
            ComboPreClustering combo = new ComboPreClustering(noeudEquilibrage);
            comboPreClusterings.add(combo);
        }

        MinMaxCalcul<ComboPreClustering> minMaxCalcul = new MinMaxCalcul<>();
        minMaxCalcul.calculerMinMax(0, Float.MIN_VALUE, comboPreClusterings);

        
        
        float[] minValeurs = minMaxCalcul.getMinValeurs();
        float[] maxValeurs = minMaxCalcul.getMaxValeurs();

        for (ComboPreClustering comboEquilibrage : comboPreClusterings) {
            comboEquilibrage.activerMinMaxNormalisation(minValeurs, maxValeurs);
        }

        return comboPreClusterings;
    }

    public void transformer() {
        
        double[][] data = matriceOrigine();

        
        
        RealMatrix matrix = new Array2DRowRealMatrix(data);

        
        Covariance covariance = new Covariance(matrix);
        RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();

        
        EigenDecomposition eigenDecomposition = new EigenDecomposition(covarianceMatrix);

        
        double[] eigenvalues = eigenDecomposition.getRealEigenvalues();
        RealMatrix eigenvectors = eigenDecomposition.getV();

        
        
        double totalEigenvalues = 0.0;
        for (double eigenvalue : eigenvalues) {
            totalEigenvalues += eigenvalue;
        }

        
        int axesPrisEnCompte = 0;
        for (int i = 0; i < eigenvalues.length; i++) {
            double varianceExplained = (eigenvalues[i] / totalEigenvalues);

            if (i == 0 || varianceExplained > MIN_PCT_VARIANCE_AXE) {
                axesPrisEnCompte++;
            }
            else if (i > 10) break;
        }

        
        
        RealMatrix transformedData = matrix.multiply(eigenvectors);
        int compte = 0;
        for (double[] pointSortie : transformedData.getData()) {
            ComboPreClustering comboPreClustering = donnees.get(compte++);
            double[] valeursAxesRetenus = Arrays.copyOfRange(pointSortie, 0, axesPrisEnCompte);
            comboPreClustering.setDonneesClusterisables(valeursAxesRetenus);
            comboPreClustering.normalisationActivee(false);
        }
    }

    
    private double[][] matriceOrigine() {
        double[][] matriceOrigine = new double[donnees.size()][];

        for (int i = 0; i < donnees.size(); i++) {
            float[] valeurs = donnees.get(i).valeursNormalisees();
            matriceOrigine[i] = new double[valeurs.length];
            for (int j = 0; j < valeurs.length; j++) {
                matriceOrigine[i][j] = valeurs[j];
            }
        }

        return matriceOrigine;
    }

    
    public List<ComboPreClustering> getDonnesTransformees() {
        return donnees;
    }
}
