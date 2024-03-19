package analyzor.modele.poker;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class ComboIsoTest {
    @Test
    void hashCodeCorrespondACode() {
        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            assertEquals(comboIso.intComboIso(), comboIso.hashCode());
        }
    }

    @Test
    void identifiantUniquePourChaqueCombo() {
        int nCombos = GenerateurCombos.combosIso.size();

        for (int i = 0; i < nCombos; i++) {
            for (int j = 0; j < nCombos; j++) {
                if (i == j) continue;
                ComboIso combo1 = GenerateurCombos.combosIso.get(i);
                ComboIso combo2 = GenerateurCombos.combosIso.get(j);

                assertNotEquals(combo1.intComboIso(), combo2.intComboIso());
            }
        }
    }

    @Test
    void tousLesCombosIsoOntUnEquivalentReel() {
        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            boolean trouve = false;
            for (ComboReel comboConverti : comboIso.toCombosReels()) {
                for (ComboReel comboReel : GenerateurCombos.combosReels) {
                    if (comboConverti.toInt() == comboReel.toInt()) {
                        trouve = true;
                    }
                }
            }
            assertTrue(trouve);
        }
    }

    @Test
    void conversionVersComboReelMarcheDansLesDeuxSens() {
        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            for (ComboReel comboReel : comboIso.toCombosReels()) {
                ComboIso comboRecree = new ComboIso(comboReel);
                assertEquals(comboIso, comboRecree);
            }
        }
    }

    @Test
    void conversionAucunComboReelNestLeMeme() {
        List<ComboReel> combosGeneres = new ArrayList<>();
        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            for (ComboReel comboReel : comboIso.toCombosReels()) {
                assertFalse(combosGeneres.contains(comboReel));
                combosGeneres.add(comboReel);
            }
        }
    }
}
