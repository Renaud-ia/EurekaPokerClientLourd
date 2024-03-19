package analyzor;

/**
 * classe privée qui permet de visualiser un temps d'avancement non linéaire
 * il faut autant de petites tâches que de grandes tâches
 */
public class ProgressionNonLineaireTest {
    // plus alpha est élevé plus les premières tâches auront du poids
    private static final float valeurAlpha = 1;
    private static final int RATIO_GRANDE_PETITE_TACHE = 10;
    private int nMaximumPonderee;
    private int iterationActuelle;
    public ProgressionNonLineaireTest() {
        iterationActuelle = 0;
    }

    private void fixerNombreIterations(int nMaximumIterations) {
        this.nMaximumPonderee = (nMaximumIterations * (RATIO_GRANDE_PETITE_TACHE + 1)) / 2;
    }

    /**
     * publier un petit incrément = tâche légère
     * @return la valeur cumulée de l'avancement
     */
    private int incrementerPetitAvancement() {
        iterationActuelle += 1;
        return iterationActuelle;
    }

    /**
     * publier un gros incrément = tâche lourde
     * @return la valeur cumulée de l'avancement
     */
    private int incrementerGrandAvancement() {
        iterationActuelle += RATIO_GRANDE_PETITE_TACHE;
        return iterationActuelle;
    }

    /**
     * @return valeur totale de l'avancement mappé
     */
    private float getPourcentageAjuste(int iterationsCumulees) {
        float valeurMappee = mapperValeurEntreZeroEtCinq(iterationsCumulees);
        return (float) exponentielleInversee(valeurMappee);
    }

    /**
     * on mappe la valeur entre 0 et 5 car exp inverse de 0 vaut 1 et exp inverse de 5 vaut presque 0
     */
    private float mapperValeurEntreZeroEtCinq(int valeurIteration) {
        return ((float) valeurIteration / nMaximumPonderee) * 5;
    }

    private double exponentielleInversee(float x) {
        return (1 - Math.exp(- valeurAlpha * x));
    }


    public static void main(String[] args) {
        final int nIterations = 80;
        ProgressionNonLineaireTest progressionNonLineaireTest = new ProgressionNonLineaireTest();
        progressionNonLineaireTest.fixerNombreIterations(nIterations);

        for (int i = 0; i < (nIterations / 2); i++) {
            int iterationPonderee = progressionNonLineaireTest.incrementerPetitAvancement();
            System.out.println("VALEUR AVANCEMENT : " + progressionNonLineaireTest.getPourcentageAjuste(iterationPonderee));

            int iterationPonderee2 = progressionNonLineaireTest.incrementerGrandAvancement();
            System.out.println("VALEUR AVANCEMENT : " + progressionNonLineaireTest.getPourcentageAjuste(iterationPonderee2));
        }
    }

}
