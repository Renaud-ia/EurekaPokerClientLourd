package analyzor.vue.donnees;

public class DTOJoueur {
    private final int indexModele;
    private final String nomPosition;
    private final boolean hero;
    private final float bounty;
    private final float stackDepart;

    public DTOJoueur(int index, String nomPosition, boolean hero, float bounty, float stackDepart) {
        this.indexModele = index;
        this.nomPosition = nomPosition;
        this.hero = hero;
        this.bounty = bounty;
        this.stackDepart = stackDepart;
    }

    public int getIndexModele() {
        return indexModele;
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
