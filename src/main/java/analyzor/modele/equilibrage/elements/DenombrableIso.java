package analyzor.modele.equilibrage.elements;

import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.evaluation.EquiteFuture;

public class DenombrableIso extends ComboDenombrable {
    private final ComboIso comboIso;
    public DenombrableIso(ComboIso comboIso, float pCombo, EquiteFuture equiteFuture, float equite) {
        super(pCombo, equiteFuture, equite);
        this.comboIso = comboIso;
        // fait doublon
        this.pCombo = comboIso.getValeur();
    }
}
