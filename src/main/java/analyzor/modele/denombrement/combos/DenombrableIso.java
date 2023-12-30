package analyzor.modele.denombrement.combos;

import analyzor.modele.denombrement.combos.ComboDenombrable;
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

    @Override
    public int hashCode() {
        return comboIso.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DenombrableIso)) return false;
        return this.comboIso.equals( ((DenombrableIso) o).comboIso);
    }

    public ComboIso getCombo() {
        return comboIso;
    }
}
