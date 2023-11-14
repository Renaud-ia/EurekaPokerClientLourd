package analyzor.modele.showdown;

import analyzor.modele.poker.RangeDenombrable;
import analyzor.modele.poker.RangeDynamique;
import analyzor.modele.poker.RangeIso;

public class ShowdownFactory {
    public static EstimateurShowdown creeEstimateur(RangeDenombrable range) {
        if (range instanceof RangeIso) {
            return new ShowdownIso();
        }
        else if (range instanceof RangeDynamique) {
            return new ShowdownDynamique();
        }

        else {
            throw new IllegalArgumentException("Cette range n'est pas dénombrable");
        }
    }
}
