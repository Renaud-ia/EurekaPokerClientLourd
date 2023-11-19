package analyzor.modele.equilibrage.elements;

import analyzor.modele.poker.ComboDynamique;
import analyzor.modele.poker.evaluation.EquiteFuture;

public class DenombrableDynamique extends ComboDenombrable {
    private ComboDynamique comboDynamique;

    protected DenombrableDynamique(float pCombo, EquiteFuture equiteFuture, float equite) {
        super(pCombo, equiteFuture, equite);
    }
}
