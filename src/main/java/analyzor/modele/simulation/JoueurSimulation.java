package analyzor.modele.simulation;

import analyzor.modele.poker.ComboIso;

import java.util.Objects;

public class JoueurSimulation {
    private final int indexJoueur;
    private final String position;
    private float stackDepart;
    private Float bounty;
    private boolean hero;

    JoueurSimulation(int indexJoueur, String position) {
        this.indexJoueur = indexJoueur;
        this.position = position;
    }
    void setStack(float stack) {
        this.stackDepart = stackDepart;
    }

    void setBounty(Float bounty) {
        this.bounty = bounty;
    }

    void setHero(boolean valeur) {
        this.hero = valeur;
    }

    public boolean hasBounty() {
        return bounty == null;
    }

    public float getBounty() {
        return Objects.requireNonNullElse(bounty, 0f);
    }

    public int getIndex() {
        return indexJoueur;
    }

    public String getNomPosition() {
        return position;
    }

    public boolean getHero() {
        return hero;
    }

    public float getStackDepart() {
        return stackDepart;
    }
}
