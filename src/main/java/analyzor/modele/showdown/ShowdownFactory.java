package analyzor.modele.showdown;

import analyzor.modele.equilibrage.elements.ComboDenombrable;
import analyzor.modele.equilibrage.elements.DenombrableDynamique;
import analyzor.modele.equilibrage.elements.DenombrableIso;

public class ShowdownFactory {
    public static EstimateurShowdown creeEstimateur(ComboDenombrable combo) {
        if (combo instanceof DenombrableIso) {
            return new ShowdownIso();
        }
        else if (combo instanceof DenombrableDynamique) {
            return new ShowdownDynamique();
        }

        else {
            throw new IllegalArgumentException("Cette range n'est pas dénombrable");
        }
    }
}
