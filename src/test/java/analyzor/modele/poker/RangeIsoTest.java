package analyzor.modele.poker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RangeIsoTest {
    @Test
    void initialisation() {
        RangeIso rangeVide = new RangeIso();
        rangeVide.rangeVide();
        assertEquals(169, rangeVide.getCombos().size());
        assertEquals(0, rangeVide.nCombos());

        RangeIso rangePleine = new RangeIso();
        rangePleine.remplir();
        assertEquals(169, rangePleine.getCombos().size());
        assertEquals(1326, rangePleine.nCombos());
    }


}
