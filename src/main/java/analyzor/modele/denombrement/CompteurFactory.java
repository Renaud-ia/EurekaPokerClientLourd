package analyzor.modele.denombrement;

import analyzor.modele.poker.RangeDenombrable;
import analyzor.modele.poker.RangeDynamique;
import analyzor.modele.poker.RangeIso;

public class CompteurFactory {
    public static CompteurRange creeCompteur(RangeDenombrable range) {
        if (range instanceof RangeIso) {
            return new CompteurIso();
        }
        else if (range instanceof RangeDynamique) {
            return new CompteurDynamique();
        }

        else {
            throw new IllegalArgumentException("Cette range n'est pas dénombrable");
        }
    }
}
