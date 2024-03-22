package analyzor.modele.poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RangeReelle {
    /**
    utilisée seulement pour calcul d'équité ?

    Stocke les combos réels d'une range (=1326) sous forme d'un index binaire
                         CARTE :
                        bxRRRRSS
                1) R = 4 bits pour codage rank
                2) S = 2 bits pour codage suit

    Indexation ultra rapide, conversion facile dans les deux sens
    Possibilité de supprimer des cartes et les combos associés
    Tirage au sort de combos au hasard
    Sensible à l'ordre des cartes => trier les combos en amont
    Range est exprimée en % du combo
     */

    private static final int COMBO_MAX;
    static  {
        COMBO_MAX = ((Carte.CARTE_MAX << Carte.N_BITS_CARTE) | (Carte.CARTE_MAX)) + 1;
    }
    private static final Random rand = new Random();

    private final float[] combos;

    //constructeurs

    /**
     * création d'une range vide, il faut la remplir séparément
     * utile pour construire des ranges manuellement (ex top range..)
     */
    public RangeReelle() {
        this.combos = new float[COMBO_MAX];
    }

    /**
     * construit une RangeReelle à partir d'une RangeIso
     * important, affecte la même proportion à un ComboReel qu'à un ComboIso (le "poids" vient du nombre de combos correspondants)
     * @param rangeIso RangeIso exprimée en % du combo
     */
    public RangeReelle(RangeIso rangeIso) {
        this.combos = new float[COMBO_MAX];
        List<ComboIso> listCombosIso = rangeIso.getCombos();
        for (ComboIso comboIso : listCombosIso) {
            List<ComboReel> listCombosReels = comboIso.toCombosReels();
            float valeur = comboIso.getValeur();
            for (ComboReel comboReel : listCombosReels) {
                this.combos[comboReel.toInt()] = valeur;
            }
        }
    }

    /**
     * convertit une RangeDynamique en RangeReelle
     */
    public RangeReelle(RangeDynamique rangeDynamique, Board board) {
        this.combos = new float[COMBO_MAX];
        //TODO
    }

    /**
     * utilisé pour cloner
     */
    private RangeReelle(float[] combos) {
        this.combos = combos;
    }

    //méthodes publiques
    public void remplir() {
        List<ComboReel> listCombosReels = GenerateurCombos.combosReels;
        for (ComboReel comboReel : listCombosReels) {
            this.combos[comboReel.toInt()] = 1;
        }
    }

    /**
     * multiplie une RangeDynamique à une RangeRéelle
     * permet de générer des séquences d'action indépendantes lors de la simulation
     */
    public void multiplier(RangeDynamique rangeDynamique) {
        //TODO
    }

    public void retirerCarte(Carte carteRetiree) {
        // on s'en fout si les combos n'existent jamais
        // surement plus rapide et plus sur que de comparer les cartes
        for (int carteRandom= 0; carteRandom <= Carte.CARTE_MAX; carteRandom++) {
            int combo1 = (carteRandom << Carte.N_BITS_CARTE) | carteRetiree.toInt();
            int combo2 = (carteRetiree.toInt() << Carte.N_BITS_CARTE) | carteRandom;

            this.combos[combo1] = 0;
            this.combos[combo2] = 0;
        }
    }

    /**
     * @return une copie profonde de la range copiée
     */
    public RangeReelle copie() {
        float[] copieCombos = this.combos.clone();
        return new RangeReelle(copieCombos);
    }

    public List<ComboReel> obtenirEchantillon(int nEchantillons) {
        // Liste pour stocker les poids cumulatifs
        List<Double> cumulativeWeights = new ArrayList<>();
        double totalSum = 0;

        // Calcul des poids cumulatifs
        for (float value : combos) {
            totalSum += value;
            cumulativeWeights.add(totalSum);
        }

        // Liste pour stocker les indices sélectionnés
        List<ComboReel> selectedIndices = new ArrayList<>();

        for (int i = 0; i < nEchantillons; i++) {
            double randomNum = rand.nextDouble() * totalSum;
            int index = binarySearch(cumulativeWeights, randomNum);
            selectedIndices.add(new ComboReel(index));
        }

        return selectedIndices;
    }

    private int binarySearch(List<Double> cumulativeWeights, double value) {
        int low = 0;
        int high = cumulativeWeights.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (value < cumulativeWeights.get(mid)) {
                high = mid - 1;
            } else if (value > cumulativeWeights.get(mid)) {
                low = mid + 1;
            } else {
                return mid; // Correspondance exacte (peu probable dans le contexte des nombres flottants)
            }
        }

        return low; // La position la plus proche si pas de correspondance exacte
    }

    /**
     * @return nombre de combos effectivement présents dans range (>0)
     */
    public int nCombos() {
        int nCombos = 0;
        for (float weight : combos) {
            if (weight > 0) nCombos++;
        }
        return nCombos;
    }

    /**
     * ajout manuel d'un combo à la range
     * @param i code binaire du ComboReel sous forme d'entier
     */
    public void ajouterCombo(int i) {
        combos[i] = 1;
    }

    public float valeurCombo(ComboReel comboReel) {
        return this.combos[comboReel.toInt()];
    }
}
