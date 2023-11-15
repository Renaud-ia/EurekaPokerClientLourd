package analyzor.modele.equilibrage.elements;

import analyzor.modele.poker.ComboIso;

public class DenombrableIso extends ComboDenombrable {
    private final ComboIso comboIso;
    public DenombrableIso(ComboIso comboIso) {
        this.comboIso = comboIso;
        // fait doublon
        this.pCombo = comboIso.getValeur();
    }
}
