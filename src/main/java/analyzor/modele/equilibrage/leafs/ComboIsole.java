package analyzor.modele.equilibrage.leafs;

import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.Strategie;

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

    @Override
    protected float probabiliteChangement(Strategie strategie, int indexActuel, int sensChangement) {
        return strategie.probaInterne(indexActuel, sensChangement);
    }

    public ComboDenombrable getComboDenombrable() {
        return combo;
    }

    @Override
    public String toString() {
        return "[COMBO ISOLE : " + combo + "]";
    }
}
