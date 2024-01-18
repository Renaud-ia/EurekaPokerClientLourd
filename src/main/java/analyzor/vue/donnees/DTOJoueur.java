package analyzor.vue.donnees;

import analyzor.modele.simulation.JoueurSimulation;
import analyzor.modele.simulation.TablePoker;

public class DTOJoueur {
    private final TablePoker.JoueurTable joueurSimulation;
    private final String nomPosition;
    private final boolean hero;
    private final float bounty;
    private final float stackDepart;

    public DTOJoueur(TablePoker.JoueurTable joueurSimulation, String nomPosition, boolean hero, float bounty, float stackDepart) {
        this.joueurSimulation = joueurSimulation;
        this.nomPosition = nomPosition;
        this.hero = hero;
        this.bounty = bounty;
        this.stackDepart = stackDepart;
    }

    public TablePoker.JoueurTable getJoueurModele() {
        return joueurSimulation;
    }

    public float getStack() {
        return stackDepart;
    }

    public boolean getHero() {
        return hero;
    }

    public float getBounty() {
        return bounty;
    }

    public String getNom() {
        return nomPosition;
    }
}
