package analyzor.modele.equilibrage.leafs;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.equilibrage.Enfant;
import analyzor.modele.equilibrage.NoeudEquilibrage;
import analyzor.modele.poker.evaluation.EquiteFuture;

public abstract class ComboDenombrable extends ObjetClusterisable implements Enfant {
    float pCombo;
    private int[] observations;
    private float[] pShowdowns;
    private final EquiteFuture equiteFuture;
    private final float equite;
    private NoeudEquilibrage parent;
    private int[] strategie;
    private float[][] probabilites;

    // important : le fold ne doit pas être compris dans nombre d'actions
    protected ComboDenombrable(float pCombo, EquiteFuture equiteFuture, int nombreActions) {
        this.pCombo = pCombo;
        this.equiteFuture = equiteFuture;
        this.equite = equiteFuture.getEquite();
        this.observations = new int[nombreActions];
        this.pShowdowns = new float[nombreActions];
        // on rajoute le fold
        this.strategie = new int[nombreActions + 1];
        this.probabilites = new float[nombreActions + 1][];
    }

    public void incrementerObservation(int indexAction) {
        if (indexAction > (observations.length - 1)) throw new IllegalArgumentException("L'index dépasse la taille max");
        observations[indexAction]++;
    }

    public void setShowdown(int indexAction, float valeur) {
        if (indexAction > (pShowdowns.length - 1)) throw new IllegalArgumentException("L'index dépasse la taille max");
        pShowdowns[indexAction] = valeur;
    }

    public void initialiserStrategie() {
        //todo
    }


    public void appliquerChangementStrategie() {
        //todo
    }

    @Override
    public float[] valeursClusterisables() {
        return equiteFuture.valeursClusterisables();
    }

    public float getEquite() {
        return equite;
    }

    @Override
    public EquiteFuture getEquiteFuture() {
        return equiteFuture;
    }

    @Override
    public int[] getStrategie() {
        return strategie;
    }

    @Override
    public int getEffectif() {
        return 1;
    }

    float getPCombo() {
        return pCombo;
    }

    int[] getObservations() {
        return observations;
    }

    float[] getShowdowns() {
        return pShowdowns;
    }

    void setProbaAction(int indexAction, float[] probaDiscretisees) {
        if (indexAction >= probabilites.length - 1)
            throw new IllegalArgumentException("L'index de l'action dépasse");
        probabilites[indexAction] = probaDiscretisees;
    }

    public void setProbaFold(float[] probaDiscretisees) {
        probabilites[probabilites.length - 1] = probaDiscretisees;
    }
}
