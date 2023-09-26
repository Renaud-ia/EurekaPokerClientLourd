package analyzor.modele.simulation;

import analyzor.modele.poker.ComboIso;

class Joueur {
    private String position;
    private int stack;
    private Float bounty = null;
    private boolean hero = false;
    private ComboIso combo = null;

    public Joueur(String position) {
        this.position = position;
    }
    public void setStack(int stack) {
        this.stack = stack;
    }
}
