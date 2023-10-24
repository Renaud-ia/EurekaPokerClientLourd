package analyzor.modele.poker.evaluation;

/**
 * stocke les résultats d'équités RangexBoard vs Ranges
 * calcule les distances entre deux matrices
 */
public class MatriceEquite {
    private final int nPercentiles;
    private int indexLigne;
    private final float[][] percentiles;
    private final float[][] resultatsTemporaires;
    protected MatriceEquite(int nPercentiles, int longueurMatrice) {
        indexLigne = 0;
        this.nPercentiles = nPercentiles;
        resultatsTemporaires = new float[longueurMatrice][nPercentiles - 1];
        percentiles = new float[nPercentiles - 1][nPercentiles - 1];
    }

    /**
     * @param resultats : liste de résultats bruts (non triés)
     */
    protected void ajouterResultatsRiver(float[] resultats) {
        float[] percentiles = Percentiles.calculerPercentiles(resultats, nPercentiles);
        resultatsTemporaires[indexLigne++] = percentiles;
    }

    /**
     * on va calculer les percentiles par colonne
     */
    protected void remplissageFini() {
        for (int indexColonne = 0; indexColonne < resultatsTemporaires[0].length; indexColonne++) {
            float[] resultsColonne = Percentiles.calculerPercentiles(obtenirColonne(indexColonne), nPercentiles);
            percentiles[indexColonne] = resultsColonne;
        }
    }

    public float distance(MatriceEquite autreMatrice) {
        //todo
        return 0f;
    }

    private float[] obtenirColonne(int indexColonne) {
        float[] colonne = new float[resultatsTemporaires.length];

        for (int i = 0; i < resultatsTemporaires.length; i++) {
            colonne[i] = resultatsTemporaires[i][indexColonne];
        }

        return colonne;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("****DEBUT MATRICE****\n");
        for (int i = 0; i < percentiles.length; i++) {
            for (int j = 0; j < percentiles[i].length; j++) {
                sb.append(percentiles[i][j]).append(" ");
            }
            sb.append("\n");
        }
        sb.append("****FIN MATRICE****");

        return sb.toString();
    }
}
