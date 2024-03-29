package analyzor.modele.poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RangeReelle {


    private static final int COMBO_MAX;
    static  {
        COMBO_MAX = ((Carte.CARTE_MAX << Carte.N_BITS_CARTE) | (Carte.CARTE_MAX)) + 1;
    }
    private static final Random rand = new Random();

    private final float[] combos;




    public RangeReelle() {
        this.combos = new float[COMBO_MAX];
    }


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


    public RangeReelle(RangeDynamique rangeDynamique, Board board) {
        this.combos = new float[COMBO_MAX];

    }


    private RangeReelle(float[] combos) {
        this.combos = combos;
    }


    public void remplir() {
        List<ComboReel> listCombosReels = GenerateurCombos.combosReels;
        for (ComboReel comboReel : listCombosReels) {
            this.combos[comboReel.toInt()] = 1;
        }
    }


    public void multiplier(RangeDynamique rangeDynamique) {

    }

    public void retirerCarte(Carte carteRetiree) {


        for (int carteRandom= 0; carteRandom <= Carte.CARTE_MAX; carteRandom++) {
            int combo1 = (carteRandom << Carte.N_BITS_CARTE) | carteRetiree.toInt();
            int combo2 = (carteRetiree.toInt() << Carte.N_BITS_CARTE) | carteRandom;

            this.combos[combo1] = 0;
            this.combos[combo2] = 0;
        }
    }


    public RangeReelle copie() {
        float[] copieCombos = this.combos.clone();
        return new RangeReelle(copieCombos);
    }

    public List<ComboReel> obtenirEchantillon(int nEchantillons) {

        List<Double> cumulativeWeights = new ArrayList<>();
        double totalSum = 0;


        for (float value : combos) {
            totalSum += value;
            cumulativeWeights.add(totalSum);
        }


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
                return mid;
            }
        }

        return low;
    }


    public int nCombos() {
        int nCombos = 0;
        for (float weight : combos) {
            if (weight > 0) nCombos++;
        }
        return nCombos;
    }


    public void ajouterCombo(int i) {
        combos[i] = 1;
    }

    public float valeurCombo(ComboReel comboReel) {
        return this.combos[comboReel.toInt()];
    }
}
