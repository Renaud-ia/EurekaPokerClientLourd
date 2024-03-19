package analyzor.modele.poker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class GenerateurCombosTest {
    @Test
    void nombreCombosIsoVaut169() {
        assertEquals(GenerateurCombos.combosIso.size(), 169);
    }
    @Test
    void aucunComboIsoIdentique() {
        int nombreCombos = GenerateurCombos.combosIso.size();
        for (int i = 0; i < nombreCombos; i++) {
            for (int j = 0; j < nombreCombos; j++) {
                if (i == j) continue;
                ComboIso comboIso1 = GenerateurCombos.combosIso.get(i);
                ComboIso comboIso2 = GenerateurCombos.combosIso.get(j);

                assertNotEquals(comboIso1, comboIso2);
            }
        }
    }

    @Test
    void nombreCombosReelVaut1326() {
        assertEquals(GenerateurCombos.combosReels.size(), 1326);
    }
    @Test
    void aucunComboReelIdentique() {
        int nombreCombos = GenerateurCombos.combosReels.size();
        for (int i = 0; i < nombreCombos; i++) {
            for (int j = 0; j < nombreCombos; j++) {
                if (i == j) continue;
                ComboReel combo1 = GenerateurCombos.combosReels.get(i);
                ComboReel combo2 = GenerateurCombos.combosReels.get(j);

                assertNotEquals(combo1, combo2);
            }
        }
    }
}
