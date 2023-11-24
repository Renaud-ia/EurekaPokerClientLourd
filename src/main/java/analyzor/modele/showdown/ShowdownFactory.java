package analyzor.modele.showdown;

import analyzor.modele.denombrement.elements.ComboDenombrable;
import analyzor.modele.denombrement.elements.DenombrableDynamique;
import analyzor.modele.denombrement.elements.DenombrableIso;

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
