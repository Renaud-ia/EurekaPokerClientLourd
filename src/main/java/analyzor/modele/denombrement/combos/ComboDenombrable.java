package analyzor.modele.denombrement.combos;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.evaluation.EquiteFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * classe qui sert de base pour le dénombrement des combos
 */
public abstract class ComboDenombrable {
    private final static Logger logger = LogManager.getLogger(ComboDenombrable.class);
    float pCombo;
    private final int[] observations;
    private final float[] pShowdowns;
    private final EquiteFuture equiteFuture;
    private final float equite;
    private float[] strategie;
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
        logger.trace("Incrémentation de :  " + this);
        logger.trace("Action d'index " + indexAction + " vaut " + observations[indexAction]);
    }

    public void setShowdown(int indexAction, float valeur) {
        if (indexAction > (pShowdowns.length - 1)) throw new IllegalArgumentException("L'index dépasse la taille max");
        pShowdowns[indexAction] = valeur;
        logger.trace("%SHOWDOWN fixé (" + this + ") pour action d'index " + indexAction + " : " + valeur);
    }


    public float getEquite() {
        return equite;
    }

    public EquiteFuture getEquiteFuture() {
        return equiteFuture;
    }

    public int getEffectif() {
        return 1;
    }


    public float getPCombo() {
        return pCombo;
    }

    public int nObservations() {
        return observations.length;
    }

    public int[] getObservations() {
        return observations;
    }

    public float[] getShowdowns() {
        return pShowdowns;
    }

    public void setStrategie(float[] strategie) {
        this.strategie = strategie;
    }

    public float[] getStrategie() {
        return strategie;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);

    // méthode de secours quand une seule action
    public void setStrategieUnique() {
        strategie = new float[1];
        strategie[0] = 1;
    }
}
