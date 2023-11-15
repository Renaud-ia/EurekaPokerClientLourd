package analyzor.modele.denombrement;

import analyzor.modele.equilibrage.elements.ComboDenombrable;
import analyzor.modele.equilibrage.elements.DenombrableDynamique;
import analyzor.modele.equilibrage.elements.DenombrableIso;

public class CompteurFactory {
    public static CompteurRange creeCompteur(ComboDenombrable combo) {
        if (combo instanceof DenombrableIso) {
            return new CompteurIso();
        }
        else if (combo instanceof DenombrableDynamique) {
            return new CompteurDynamique();
        }

        else {
            throw new IllegalArgumentException("Cette range n'est pas dénombrable");
        }
    }
}
