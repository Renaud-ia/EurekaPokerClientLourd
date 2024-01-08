package analyzor.modele.simulation;

import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.poker.RangeIso;
import analyzor.vue.donnees.DTOJoueur;

import java.util.HashMap;
import java.util.List;

public class SimuSituation {
    // stocke la liste des situations gardées en mémoire par la table
    private int index;
    private JoueurSimulation joueur;
    private float stack;
    private boolean actionFixee = false;
    private List<SimuAction> actions;
    private HashMap<SimuAction, RangeIso> rangesActions;
    private NoeudAbstrait noeudAbstrait;
    private float pot;
    private float potBounty;

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

    public NoeudAbstrait getNoeudAbstrait() {
        return noeudAbstrait;
    }

    public float getPot() {
        return pot;
    }

    public float getPotBounty() {
        return potBounty;
    }

    public void deselectionnerAction() {
    }

    public void fixerAction(int indexAction) {
    }

    public boolean actionFixee() {
        return false;
    }

    public int fixerActionParDefaut() {
        return 0;
    }

    public SimuAction getActionSelectionnee() {
        return null;
    }
}
