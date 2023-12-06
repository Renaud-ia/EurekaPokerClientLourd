package analyzor.modele.poker.evaluation;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.equilibrage.Enfant;
import analyzor.modele.parties.TourMain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

/**
 * stocke l'équité future (flop, turn et river) d'une main
 * sert de référence pour générer des ComboDynamique
 * attention pour l'encodage ne pas dépasser 63 bits = 9 percentiles * 7 bits
 */
public class EquiteFuture extends ObjetClusterisable implements Serializable {
    private static EnumMap<TourMain.Round, Integer> indexParStreet;
    static {
        indexParStreet = new EnumMap<>(TourMain.Round.class);
        indexParStreet.put(TourMain.Round.RIVER, 0);
        indexParStreet.put(TourMain.Round.TURN, 1);
        indexParStreet.put(TourMain.Round.FLOP, 2);
    }
    private transient TourMain.Round round;
    private float[][] equites = new float[3][];
    private int nPercentiles;
    private float equite;
    public EquiteFuture(int nPercentiles) {
        round = TourMain.Round.RIVER;
        this.nPercentiles = nPercentiles;
    }

    EquiteFuture(float[][] equites, int nPercentiles) {
        this.equites = equites;
        this.nPercentiles = nPercentiles;
    }

    // constructeur utilisé pour faire la moyenne
    public EquiteFuture(List<EquiteFuture> equites, List<Float> poids) {
        if (equites.size() != poids.size()) throw new IllegalArgumentException("Pas la même dimension");

        this.nPercentiles = equites.get(0).nPercentiles;

        for (int i = 0; i < equites.size(); i++) {
            this.ajouter(equites.get(i), poids.get(i));
        }
        this.diviser((float) poids.stream().mapToDouble(Float::doubleValue).sum());
    }

    /**
     * @param resultats : liste de résultats bruts (non triés)
     */
    public void ajouterResultatStreet(float[] resultats) {
        // on commence par remplir la river comme ça la première colonne est toujours remplie
        float[] percentiles = Percentiles.calculerPercentiles(resultats, nPercentiles);
        int index = indexParStreet.get(round);
        equites[index] = percentiles;

        if (round == TourMain.Round.RIVER) calculerEquite(resultats);

        round = round.precedent();
    }

    private void calculerEquite(float[] resultats) {
        this.equite = 0;
        for (int i = 0; i < resultats.length; i++) {
            equite += resultats[i];
        }
        this.equite /= resultats.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EQUITE A VENIR (").append(round.toString()).append(") : ");
        for (int i = equites.length - 1 ; i >= 0; i--) {
            if (equites[i] == null) continue;
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
    public float[] aPlat() {
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

    public float getEquite() {
        return equite;
    }

    // on sait jamais si ça peut servir (=> valeur dans hashmap)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EquiteFuture)) return false;
        EquiteFuture equiteComparee = (EquiteFuture) o;
        if (this.aPlat().length != equiteComparee.aPlat().length) return false;
        for (int i = 0; i < this.aPlat().length; i++) {
            if (this.aPlat()[i] != equiteComparee.aPlat()[i]) return false;
        }
        return true;
    }

    public EquiteFuture multiPonderee(Enfant enfant2, float ponderation) {
        float[][] nouvellesEquites = new float[equites.length][];

        float[][] equitesEnfant2 = enfant2.getEquiteFuture().equites;

        for (int i = 0; i < equites.length; i++) {
            // on ne parcout que les colonnes remplies
            if (equites[i] == null) {
                if (equitesEnfant2[i] != null)
                    throw new IllegalArgumentException("Les deux équités n'ont pas la même dimension");
                continue;
            }
            nouvellesEquites[i] = new float[equites[i].length];
            for (int j = 0; j < equites[i].length; j++) {
                nouvellesEquites[i][j] = (equites[i][j] + ponderation * equitesEnfant2[i][j]) / (1 + ponderation);
            }
        }

        return new EquiteFuture(nouvellesEquites, nPercentiles);
    }

    // méthodes utilisées pour faire une équité moyenne à partir de sommes d'équité future
    private void ajouter(EquiteFuture equiteFuture, float pCombo) {
        if (equites == null) {
            equites = equiteFuture.equites;
        }
        else {
            for (int i = 0; i < equites.length; i++) {
                // on ne parcout que les colonnes remplies
                if (equites[i] == null) {
                    if (equiteFuture.equites[i] != null)
                        throw new IllegalArgumentException("Les deux équités n'ont pas la même dimension");
                    continue;
                }
                for (int j = 0; j < equites[i].length; j++) {
                    equites[i][j] = (equites[i][j] * pCombo);
                }
            }
        }
    }

    private void diviser(float sommePCombo) {
        for (int i = 0; i < equites.length; i++) {
            // on ne parcout que les colonnes remplies
            if (equites[i] == null) {
                continue;
            }
            for (int j = 0; j < equites[i].length; j++) {
                equites[i][j] /= sommePCombo;
            }
        }
    }
}
