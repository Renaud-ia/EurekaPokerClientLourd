package analyzor.modele.denombrement;

import analyzor.modele.arbre.NoeudDenombrable;
import analyzor.modele.equilibrage.elements.ComboDenombrable;
import analyzor.modele.equilibrage.elements.DenombrableDynamique;
import analyzor.modele.equilibrage.elements.DenombrableIso;
import analyzor.modele.poker.ComboIso;

import java.util.List;

public class CompteurRange {
    public CompteurRange() {}
    public void remplirCombos(NoeudDenombrable noeudDenombrable) {
        List<ComboDenombrable> combos = noeudDenombrable.getCombosDenombrables();
        if (combos.get(0) instanceof DenombrableIso) {
            remplirCombosIso(noeudDenombrable);
        }
        else if (combos.get(0) instanceof DenombrableDynamique) {
            remplirCombosDynamiques(noeudDenombrable);
        }
        else throw new IllegalArgumentException("Les combos du noeud ne sont pas dénombrables");
    }

    private void remplirCombosDynamiques(NoeudDenombrable noeudDenombrable) {
    }

    private void remplirCombosIso(NoeudDenombrable noeudDenombrable) {
    }
}
