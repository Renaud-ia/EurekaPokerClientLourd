package analyzor.modele.poker.evaluation;

import analyzor.modele.parties.TourMain;

public class EquiteFuture {
    private TourMain.Round round;
    private float[][] equites = new float[3][];
    private int index;
    private final int nPercentiles;
    public EquiteFuture(int nPercentiles) {
        index = 0;
        round = TourMain.Round.RIVER;
        this.nPercentiles = nPercentiles;
    }

    /**
     * @param resultats : liste de résultats bruts (non triés)
     */
    public void ajouterResultatStreet(float[] resultats) {
        float[] percentiles = Percentiles.calculerPercentiles(resultats, nPercentiles);
        equites[index++] = percentiles;
        round = round.precedent();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EQUITE A VENIR (").append(round.toString()).append(") : ");
        for (int i = 0; i < equites.length; i++) {
            if (equites[i] == null) break;
            sb.append("[");
                for (int j = 0; j < equites[i].length; j++) {
                    sb.append(equites[i][j]);
                    if (j < (equites[i].length - 1)) sb.append(",");
                }
            sb.append("]");
        }

        return sb.toString();
    }

    /**
     * met à plat les équités
     */
    public float[] aPlat() {
        int colonnesRemplies = 0;
        int nombrePercentiles = 0;
        for (float[] floats : equites) {
            if (floats == null) continue;
            colonnesRemplies++;
            int nPercentilesColonne = floats.length;
            if (nombrePercentiles > 0 && nPercentilesColonne != nombrePercentiles)
                throw new RuntimeException("Les équités n'ont pas le même taille sur toutes les streets");
            nombrePercentiles = nPercentilesColonne;
        }

        float[] aPlat = new float[colonnesRemplies * nombrePercentiles];
        int index = 0;
        // on ne parcout que les colonnes remplies
        for (int i = (equites.length + 1 - colonnesRemplies); i < equites.length; i++) {
            float[] equite = equites[i];
            for (float v : equite) {
                aPlat[index++] = v;
            }
        }

        return aPlat;
    }
}
