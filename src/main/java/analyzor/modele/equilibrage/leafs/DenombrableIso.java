package analyzor.modele.equilibrage.leafs;

import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.evaluation.EquiteFuture;

public class DenombrableIso extends ComboDenombrable {
    private final ComboIso comboIso;
    public DenombrableIso(ComboIso comboIso, float pCombo, EquiteFuture equiteFuture, int nombreActions) {
        super(pCombo, equiteFuture, nombreActions);
        this.comboIso = comboIso;
        this.pCombo = pCombo;
    }

    @Override
    public String toString() {
        return comboIso.codeReduit() + "(" + (pCombo * 100) + "%)";
    }
}
