package analyzor.modele.simulation;

import analyzor.modele.poker.RangeIso;
import analyzor.vue.donnees.DTOJoueur;

import java.util.HashMap;
import java.util.List;

public class SimuSituation {
    // stocke la liste des situations gardées en mémoire par la table
    private int index;
    private JoueurSimulation joueur;
    private float stack;
    private boolean fixe = false;
    private List<SimuAction> actions;
    private HashMap<SimuAction, RangeIso> rangesActions;

    public int getIndex() {
        return index;
    }

    public JoueurSimulation getJoueur() {
        return joueur;
    }

    public float getStack() {
        return stack;
    }

    public List<SimuAction> getActions() {
        // todo important on veut l'ordre suivant : fold, call, raise par ordre de bet size, all-in
        return actions;
    }
}
