package analyzor.modele.simulation;

import analyzor.modele.poker.ComboIso;

import java.util.Objects;

public class JoueurSimulation {
    private final String position;
    private float stackDepart;
    private Float bounty;
    private boolean hero;

    JoueurSimulation(String position) {
        this.position = position;
    }
    void setStackDepart(float stack) {
        this.stackDepart = stack;
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

    public String getNomPosition() {
        return position;
    }

    public boolean estHero() {
        return hero;
    }

    public float getStackDepart() {
        return stackDepart;
    }

    //pour debug
    @Override
    public String toString() {
        return position + "(" + stackDepart + "bb)";
    }
}
