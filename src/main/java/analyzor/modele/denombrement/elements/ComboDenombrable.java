package analyzor.modele.denombrement.elements;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.poker.evaluation.EquiteFuture;

public abstract class ComboDenombrable extends ObjetClusterisable {
    float pCombo;
    private int[] observations;
    private float[] pShowdowns;
    private final EquiteFuture equiteFuture;
    private final float equite;

    protected ComboDenombrable(float pCombo, EquiteFuture equiteFuture, int nombreActions) {
        this.pCombo = pCombo;
        this.equiteFuture = equiteFuture;
        this.equite = equiteFuture.getEquite();
        this.observations = new int[nombreActions];
        this.pShowdowns = new float[nombreActions];
    }

    public void incrementerObservation(int indexAction) {
        if (indexAction > (observations.length - 1)) throw new IllegalArgumentException("L'index dépasse la taille max");
        observations[indexAction]++;
    }

    public void setShowdown(int indexAction, float valeur) {
        if (indexAction > (pShowdowns.length - 1)) throw new IllegalArgumentException("L'index dépasse la taille max");
        pShowdowns[indexAction] = valeur;
    }

    @Override
    public float[] valeursClusterisables() {
        return equiteFuture.valeursClusterisables();
    }

    public float getEquite() {
        return equite;
    }
}
