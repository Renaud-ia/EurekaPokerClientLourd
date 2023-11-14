package analyzor.modele.equilibrage.elements;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.poker.evaluation.EquiteFuture;

public abstract class ComboDenombrable extends ObjetClusterisable {
    float pCombo;
    private int[] observations;
    private float[] showdowns;
    private EquiteFuture equiteFuture;

    @Override
    public float[] valeursClusterisables() {
        return equiteFuture.aPlat();
    }
}
