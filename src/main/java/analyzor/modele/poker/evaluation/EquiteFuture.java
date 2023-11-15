package analyzor.modele.poker.evaluation;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.parties.TourMain;

import java.util.EnumMap;

/**
 * stocke l'équité future (flop, turn et river) d'une main
 * sert de référence pour générer des ComboDynamique
 * attention pour l'encodage ne pas dépasser 63 bits = 9 percentiles * 7 bits
 */
public class EquiteFuture extends ObjetClusterisable {
    private EnumMap<TourMain.Round, Integer> indexParStreet;
    private TourMain.Round round;
    private float[][] equites = new float[3][];
    private int index;
    private final int nPercentiles;
    public EquiteFuture(int nPercentiles) {
        index = 0;
        round = TourMain.Round.RIVER;
        this.nPercentiles = nPercentiles;
        indexParStreet = new EnumMap<>(TourMain.Round.class);
    }

    /**
     * @param resultats : liste de résultats bruts (non triés)
     */
    public void ajouterResultatStreet(float[] resultats) {
        // on commence par remplir la river comme ça la première colonne est toujours remplie
        float[] percentiles = Percentiles.calculerPercentiles(resultats, nPercentiles);
        equites[index] = percentiles;
        // on garde les index pour création des combos dynamiques
        indexParStreet.put(round, index);

        round = round.precedent();
        index++;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EQUITE A VENIR (").append(round.toString()).append(") : ");
        for (int i = equites.length - 1 ; i >= 0; i--) {
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

    public long getEquite(TourMain.Round round) {
        long codeEquite = 0L;

        Integer index = indexParStreet.get(round);
        if (index == null) return codeEquite;

        float[] equiteStreet = equites[index];
        int nBits = 0;
        int MAX_BITS_LONG = 63;
        // 7 bits = 128 combinaisons (équité absolue entre 0 et 100)
        int decalageBits = 7;

        for (float equite : equiteStreet) {
            // on prend les trois chiffres signicatifs
            int equiteArrondie = Math.round(equite * 100);
            codeEquite = (codeEquite << decalageBits) + equiteArrondie;
            nBits += 7;

            if (nBits >= MAX_BITS_LONG) {
                throw new RuntimeException("Pas assez de bits pour encoder");
            }
        }

        return codeEquite;
    }

    @Override
    public float[] valeursClusterisables() {
        return this.aPlat();
    }

    /**
     * met à plat les équités
     */
    private float[] aPlat() {
        int colonnesRemplies = colonnesRemplies();
        // on a vérifié que les colonnes étaient bien remplies
        int nombrePercentiles = equites[0].length;

        float[] aPlat = new float[colonnesRemplies * nombrePercentiles];
        int index = 0;

        for (float[] equite : equites) {
            // on ne parcout que les colonnes remplies
            if (equite == null) continue;
            for (float v : equite) {
                aPlat[index++] = v;
            }
        }

        return aPlat;
    }

    private int colonnesRemplies() {
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
        if (colonnesRemplies == 0) throw new RuntimeException("Aucune colonne d'équité remplie");

        return colonnesRemplies;
    }
}
