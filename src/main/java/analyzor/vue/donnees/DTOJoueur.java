package analyzor.vue.donnees;

import analyzor.modele.simulation.JoueurSimulation;

public class DTOJoueur {
    private final JoueurSimulation joueurSimulation;
    private final String nomPosition;
    private final boolean hero;
    private final float bounty;
    private final float stackDepart;

    public DTOJoueur(JoueurSimulation joueurSimulation, String nomPosition, boolean hero, float bounty, float stackDepart) {
        this.joueurSimulation = joueurSimulation;
        this.nomPosition = nomPosition;
        this.hero = hero;
        this.bounty = bounty;
        this.stackDepart = stackDepart;
    }

    public JoueurSimulation getJoueurModele() {
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
}
