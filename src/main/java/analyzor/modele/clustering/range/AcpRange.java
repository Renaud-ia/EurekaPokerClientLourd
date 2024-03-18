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

/**
 * classe utilisée pour effectuer une ACP sur les combos de la range pour réduire la dimensionnalité et faire des stats
 */
public class AcpRange {
    private final static Logger logger = LogManager.getLogger(AcpRange.class);

    // pourcentage de variance expliquée minimale d'une composante pour être prise en compte
    private final static float MIN_PCT_VARIANCE_AXE = 0.10f;
    private final static int MAX_DIMENSIONS = 5;
    private LinkedList<ComboPreClustering> donnees;
    public AcpRange() {
    }

    /**
     * ajouter les données pour l'ACP
     * on veut une LinkedList pour être surs de conserver l'ordre de la donnée
     */
    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        donnees = formaterDonnees(noeuds);
    }

    /**
     * mise en forme des données de départ
     * on normalise par min et max
     * @param noeuds comboInitiaux
     */
    private LinkedList<ComboPreClustering> formaterDonnees(List<NoeudEquilibrage> noeuds) {
        // on crée des objets spéciaux qui implémentent les bonnes méthodes
        LinkedList<ComboPreClustering> comboPreClusterings = new LinkedList<>();
        for (NoeudEquilibrage noeudEquilibrage : noeuds) {
            ComboPreClustering combo = new ComboPreClustering(noeudEquilibrage);
            comboPreClusterings.add(combo);
        }

        MinMaxCalcul<ComboPreClustering> minMaxCalcul = new MinMaxCalcul<>();
        minMaxCalcul.calculerMinMax(0, Float.MIN_VALUE, comboPreClusterings);

        // on normalise les données avec min max
        // on calcule les valeurs min et max
        float[] minValeurs = minMaxCalcul.getMinValeurs();
        float[] maxValeurs = minMaxCalcul.getMaxValeurs();

        for (ComboPreClustering comboEquilibrage : comboPreClusterings) {
            comboEquilibrage.activerMinMaxNormalisation(minValeurs, maxValeurs);
        }

        return comboPreClusterings;
    }

    public void transformer() {
        // on crée un vecteur lié aux données
        double[][] data = matriceOrigine();

        // on fait l'ACP
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

        // on sélectionne les axes qui contribuent suffisamment à la variance, au moins un
        // Calcul de la somme totale des valeurs propres
        double totalEigenvalues = 0.0;
        for (double eigenvalue : eigenvalues) {
            totalEigenvalues += eigenvalue;
        }

        // Calcul du pourcentage de variance expliquée par chaque composante principale
        int axesPrisEnCompte = 0;
        logger.debug("Pourcentage de variance expliquée par chaque composante : ");
        for (int i = 0; i < eigenvalues.length; i++) {
            double varianceExplained = (eigenvalues[i] / totalEigenvalues);
            logger.debug("Composante " + (i + 1) + ": " + varianceExplained * 100 + "%");

            if (i == 0 || varianceExplained > MIN_PCT_VARIANCE_AXE) {
                axesPrisEnCompte++;
            }
            else if (i > 10) break;
        }

        // on transforme les données dans les objets d'origine
        // Projection des données originales sur les axes de l'ACP
        RealMatrix transformedData = matrix.multiply(eigenvectors);
        int compte = 0;
        for (double[] pointSortie : transformedData.getData()) {
            ComboPreClustering comboPreClustering = donnees.get(compte++);
            double[] valeursAxesRetenus = Arrays.copyOfRange(pointSortie, 0, axesPrisEnCompte);
            comboPreClustering.setDonneesClusterisables(valeursAxesRetenus);
            comboPreClustering.normalisationActivee(false);

            logger.trace("Axes fixés pour : " + comboPreClustering.getNoeudEquilibrage());
            logger.trace("Valeurs composantes retenus : " + Arrays.toString(valeursAxesRetenus));
            logger.trace("Valeur normalisées vaut : " + Arrays.toString(comboPreClustering.valeursNormalisees()));
        }
    }

    /**
     * obligatoire de décompresser les données sous forme de matrice double
     * @return les données d'entrées sous forme de matrice
     */
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

    /**
     * récupérer les résultats
     * en soi inutile car modifie les objets initiaux mais ok
     * @return les combos avec les valeurs exprimées en ACP
     */
    public List<ComboPreClustering> getDonnesTransformees() {
        return donnees;
    }
}
