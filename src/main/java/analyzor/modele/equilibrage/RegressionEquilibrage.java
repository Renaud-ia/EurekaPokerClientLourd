package analyzor.modele.equilibrage;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.poker.evaluation.EquiteFuture;

import java.util.Arrays;

// permet de calculer l'évolution du rapport equité/stratégie
public class RegressionEquilibrage {
    private final float[] valeursEquite;
    private final float[] valeursStrategie;
    private final Float[] testStrategie;

    public RegressionEquilibrage(int nombreNoeuds) {
        valeursEquite = new float[nombreNoeuds];
        valeursStrategie = new float[nombreNoeuds];
        testStrategie = new Float[nombreNoeuds];
        resetTest();
    }

    public void setDispersionStrategie(NoeudEquilibrage noeudEquilibrage, float dispersionStragie) {
        valeursStrategie[noeudEquilibrage.getIndex()] = dispersionStragie;
    }

    public void testerValeurDispersion(NoeudEquilibrage noeudEquilibrage, float dispersionStrategie) {
        testStrategie[noeudEquilibrage.getIndex()] = dispersionStrategie;
    }

    public void setDispersionEquite(NoeudEquilibrage noeudEquilibrage, float distanceEquite) {
        valeursEquite[noeudEquilibrage.getIndex()] = distanceEquite;
    }

    /**
     * coefficient de correlation de Pearson
     */
    private float getCorrelation() {
        int n = valeursEquite.length;
        float sommeX = 0, sommeY = 0, sommeXY = 0, sommeXCarre = 0, sommeYCarre = 0;

        for (int i = 0; i < n; i++) {
            sommeX += valeursEquite[i];
            sommeY += valeursStrategie[i];
            sommeXY += valeursEquite[i] * valeursStrategie[i];
            sommeXCarre += valeursEquite[i] * valeursEquite[i];
            sommeYCarre += valeursStrategie[i] * valeursStrategie[i];
        }

        float numerateur = n * sommeXY - sommeX * sommeY;
        float denominateur = (float) Math.sqrt((n * sommeXCarre - sommeX * sommeX) * (n * sommeYCarre - sommeY * sommeY));

        if (denominateur == 0) {
            return 0; // Pour éviter la division par zéro
        }

        return numerateur / denominateur;
    }

    private float getCorrelationTest() {
        int n = valeursEquite.length;
        float sommeX = 0, sommeY = 0, sommeXY = 0, sommeXCarre = 0, sommeYCarre = 0;

        for (int i = 0; i < n; i++) {
            float valeurStrategie;
            if (testStrategie[i] == null) valeurStrategie = valeursStrategie[i];
            else valeurStrategie = testStrategie[i];

            sommeX += valeursEquite[i];
            sommeY += valeurStrategie;
            sommeXY += valeursEquite[i] * valeurStrategie;
            sommeXCarre += valeursEquite[i] * valeursEquite[i];
            sommeYCarre += valeurStrategie * valeurStrategie;
        }

        float numerateur = n * sommeXY - sommeX * sommeY;
        float denominateur = (float) Math.sqrt((n * sommeXCarre - sommeX * sommeX) * (n * sommeYCarre - sommeY * sommeY));

        if (denominateur == 0) {
            return 0; // Pour éviter la division par zéro
        }

        return numerateur / denominateur;
    }

    /**
     * retourne le facteur d'amélioration de la corrélation avec les valeurs de test
     */
    public float getAmelioration() {
        return getCorrelationTest() / getCorrelation();
    }

    // important à reset dès qu'on relance un test
    public void resetTest() {
        Arrays.fill(testStrategie, null);
    }
}
