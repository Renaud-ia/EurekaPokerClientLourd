package analyzor.modele.poker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RangeIsoTest {
    /**
     * vérifie qu'une range iso a 169 combos
     */
    @Test
    void nombreCombosValide() {
        RangeIso rangeVide = new RangeIso();
        rangeVide.rangeVide();
        assertEquals(169, rangeVide.getCombos().size());
        assertEquals(0, rangeVide.nCombos());

        RangeIso rangePleine = new RangeIso();
        rangePleine.remplir();
        assertEquals(169, rangePleine.getCombos().size());
        assertEquals(1326, rangePleine.nCombos());
    }

    /**
     * vérifie que chaque combo du générateur est présente dans une range
     */
    @Test
    void tousLesCombosPresentsDansRange() {
        RangeIso rangeIso = new RangeIso();

        // une range vide doit avoir tous les combos
        rangeIso.rangeVide();
        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            boolean trouve = false;
            for (ComboIso comboRange : rangeIso.getCombos()) {
                if (comboIso.equals(comboRange)) trouve = true;
            }
            assertTrue(trouve);
        }

        // une range pleine aussi
        rangeIso.remplir();
        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            boolean trouve = false;
            for (ComboIso comboRange : rangeIso.getCombos()) {
                if (comboIso.equals(comboRange)) trouve = true;
            }
            assertTrue(trouve);
        }
    }

    /**
     * vérifie que la multiplication produit les bons résultats
     */
    @Test
    void multiplicationRange() {
        // todo
    }


}
