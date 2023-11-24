package analyzor.modele.equilibrage.elements;

import analyzor.modele.equilibrage.elements.ComboDenombrable;
import analyzor.modele.poker.ComboDynamique;
import analyzor.modele.poker.evaluation.EquiteFuture;

public class DenombrableDynamique extends ComboDenombrable {
    private ComboDynamique comboDynamique;

    protected DenombrableDynamique(float pCombo, EquiteFuture equiteFuture, int nombreActions) {
        super(pCombo, equiteFuture, nombreActions);
    }
}
