package analyzor.modele.poker.evaluation;


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

    
    protected void ajouterResultatsRiver(float[] resultats) {
        float[] percentiles = Percentiles.calculerPercentiles(resultats, nPercentiles);
        resultatsTemporaires[indexLigne++] = percentiles;
    }

    
    protected void remplissageFini() {
        for (int indexColonne = 0; indexColonne < resultatsTemporaires[0].length; indexColonne++) {
            float[] resultsColonne = Percentiles.calculerPercentiles(obtenirColonne(indexColonne), nPercentiles);
            percentiles[indexColonne] = resultsColonne;
        }
    }

    
    public float distance(MatriceEquite autreMatrice) {
        if(this.dimension() != autreMatrice.dimension())
            throw new IllegalArgumentException("Les matrices ne font pas la même taille");

        float sumDifference = 0;
        for (int i = 0; i < percentiles.length; i++) {
            for (int j = 0; j < percentiles[0].length; j++) {
                float difference = this.valeurAt(i, j) - autreMatrice.valeurAt(i, j);
                sumDifference += difference * difference;
            }
        }

        return (float) Math.sqrt(sumDifference);
    }

    private float valeurAt(int i, int j) {
        return percentiles[i][j];
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

    public int dimension() {
        return nPercentiles - 1;
    }
}
