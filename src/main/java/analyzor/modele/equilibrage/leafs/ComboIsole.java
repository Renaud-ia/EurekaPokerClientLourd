package analyzor.modele.equilibrage.leafs;

import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.Strategie;
import analyzor.modele.poker.ComboIso;

/**
 * combo qui ne fait pas partie d'un cluster
 * indépendant sur les changements de probabilités
 */
public class ComboIsole extends NoeudEquilibrage {
    final ComboDenombrable combo;
    public ComboIsole(ComboDenombrable comboDenombrable) {
        super(comboDenombrable.getPCombo(),
                comboDenombrable.getObservations(),
                comboDenombrable.getShowdowns(),
                comboDenombrable.getEquiteFuture());

        this.combo = comboDenombrable;
    }

    public ComboDenombrable getComboDenombrable() {
        return combo;
    }

    @Override
    public String toString() {
        return "[COMBO ISOLE : " + combo + "]";
    }

    @Override
    public int hashCode() {
        return combo.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ComboIsole)) return false;
        return this.combo.equals( ((ComboIsole) o).combo);
    }

    public void fixerStrategie() {
        this.combo.setStrategie(this.getStrategieActuelle());
    }
}
