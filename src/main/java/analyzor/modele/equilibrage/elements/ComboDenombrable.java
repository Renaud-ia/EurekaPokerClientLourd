package analyzor.modele.equilibrage.elements;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.poker.evaluation.EquiteFuture;

public abstract class ComboDenombrable extends ObjetClusterisable {
    float pCombo;
    private int[] observations;
    private float[] pShowdowns;
    private final EquiteFuture equiteFuture;
    private final float equite;

    protected ComboDenombrable(float pCombo, EquiteFuture equiteFuture, float equite) {
        this.pCombo = pCombo;
        this.equiteFuture = equiteFuture;
        this.equite = equite;
    }

    @Override
    public float[] valeursClusterisables() {
        return equiteFuture.valeursClusterisables();
    }
}
