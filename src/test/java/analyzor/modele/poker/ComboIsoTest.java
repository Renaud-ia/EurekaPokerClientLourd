package analyzor.modele.poker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComboIsoTest {
    @Test
    void egalite() {
        RangeIso rangeIso = new RangeIso();
        rangeIso.rangeVide();
        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            boolean trouve = false;
            for (ComboIso comboRange : rangeIso.getCombos()) {
                if (comboIso.equals(comboRange)) trouve = true;
            }
            assertTrue(trouve);
        }
    }
}
