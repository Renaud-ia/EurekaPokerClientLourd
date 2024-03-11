package analyzor.modele.poker;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ComboReelTest {
    @Test
    void hashCodeCorrespondACode() {
        for (ComboReel comboReel : GenerateurCombos.combosReels) {
            assertEquals(comboReel.toInt(), comboReel.hashCode());
        }
    }

    @Test
    void identifiantUniquePourChaqueCombo() {
        int nCombos = GenerateurCombos.combosReels.size();

        for (int i = 0; i < nCombos; i++) {
            for (int j = 0; j < nCombos; j++) {
                if (i == j) continue;
                ComboReel combo1 = GenerateurCombos.combosReels.get(i);
                ComboReel combo2 = GenerateurCombos.combosReels.get(j);

                assertNotEquals(combo1.toInt(), combo2.toInt());
            }
        }
    }

    /**
     * garantit qu'un combo Réel est toujours égal quelque soit l'ordre de saisie des cartes
     */
    @Test
    void changerOrdreDesCartesNeChangePasIdentifiantUnique() {
        for(Character rank1 : Carte.STR_RANKS) {
            for(Character rank2 : Carte.STR_RANKS) {
                for (Character suit1 : Carte.STR_SUITS) {
                    for (Character suit2 : Carte.STR_SUITS) {
                        ComboReel comboReel = new ComboReel(rank1, suit1, rank2, suit2);
                        ComboReel comboInverse = new ComboReel(rank2, suit2, rank1, suit1);
                        assertEquals(comboInverse.toInt(), comboReel.toInt());
                    }
                }
            }
        }
    }

    @Test
    void tousLesCombosReelsOntUnEquivalentIso() {
        // test conversion vers combos reels
        for (ComboReel comboReel : GenerateurCombos.combosReels) {
            boolean trouve = false;
            for (ComboIso comboIso : GenerateurCombos.combosIso) {
                for (ComboReel equivalentReel : comboIso.toCombosReels()) {
                    if (equivalentReel.toInt() == comboReel.toInt()) {
                        trouve = true;
                    }
                }
            }
            assertTrue(trouve);
        }
    }


}
