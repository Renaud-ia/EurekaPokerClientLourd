package analyzor.modele.poker;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ComboTest {
    @Test
    void ordreInchange() {
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
    void combosIso() {
        int nombreCombos = 0;
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
            nombreCombos++;
        }
        assertEquals(nombreCombos, 169);

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

    @Test
    void combosReels() {
        List<ComboReel> combosReelsGeneres = new ArrayList<>();
        for (ComboReel comboReel : GenerateurCombos.combosReels) {
            assertFalse(combosReelsGeneres.contains(comboReel));
            combosReelsGeneres.add(comboReel);
        }

        assertEquals(combosReelsGeneres.size(), 1326);
    }

    @Test
    void valeurCombo() {
        ComboReel comboReel = new ComboReel('A', 'h', '4', 's');
        ComboReel comboReel2 = new ComboReel(13464);
        System.out.println(comboReel);
        System.out.println(comboReel2);
    }
}
