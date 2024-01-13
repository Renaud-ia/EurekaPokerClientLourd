package analyzor.modele.simulation;

import analyzor.modele.arbre.noeuds.NoeudSituation;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.poker.RangeIso;

import java.util.*;

/**
 * stocke les informations sur la situation AVANT les actions
 * stack des joueurs, si les joueurs sont actifs etc.
 * ainsi que les actions possibles
 */
public class SimuSituation {
    // stocke la liste des situations gardées en mémoire par la table
    private final NoeudSituation noeudSituation;
    private final JoueurSimulation joueur;
    private final HashMap<JoueurSimulation, Float> stacks;
    private final HashMap<JoueurSimulation, Boolean> joueurFolde;
    private final float pot;
    private final float potBounty;
    private final PriorityQueue<SimuAction> queueActions;
    private LinkedList<SimuAction> actionsTriees;
    private Integer actionSelectionnee;

    public SimuSituation(NoeudSituation noeudSituation,
                         JoueurSimulation joueur,
                         HashMap<JoueurSimulation, Float> stacks,
                         HashMap<JoueurSimulation, Boolean> joueurFolde,
                         float pot, float potBounty) {
        this.noeudSituation = noeudSituation;
        this.joueur = joueur;
        this.stacks = stacks;
        this.joueurFolde = joueurFolde;
        this.pot = pot;
        this.potBounty = potBounty;

        queueActions = new PriorityQueue<>();
    }

    // interface publique utilisée par le controleur

    public JoueurSimulation getJoueur() {
        return joueur;
    }

    public float getStack() {
        return stacks.get(joueur);
    }

    public LinkedList<SimuAction> getActions() {
        if (actionsTriees == null) {
            actionsTriees = new LinkedList<>(queueActions);
        }
        for (int i = 0; i < actionsTriees.size(); i++) {
            SimuAction action = actionsTriees.get(i);
            action.setIndex(i);
        }
        return actionsTriees;
    }

    // interface package-private pour modifier la situation

    void ajouterAction(SimuAction simuAction) {
        queueActions.add(simuAction);
    }

    void fixerAction(int indexAction) {
        actionSelectionnee = indexAction;
    }

    // return null si action déjà fixée
    Integer fixerActionParDefaut() {
        if (actionFixee()) return null;
        else fixerAction(0);
        return 0;
    }

    // interface package-private pour obtenir les infos

    float getPot() {
        return pot;
    }

    float getPotBounty() {
        return potBounty;
    }

    boolean actionFixee() {
        return actionSelectionnee != null;
    }

    SimuAction getActionActuelle() {
        return actionsTriees.get(actionSelectionnee);
    }

    HashMap<JoueurSimulation, Float> getStacks() {
        return stacks;
    }

    HashMap<JoueurSimulation, Boolean> getJoueurFolde() {
        return joueurFolde;
    }

    NoeudSituation getNoeudSituation() {
        return noeudSituation;
    }

    public void deselectionnerAction() {
        actionSelectionnee = null;
    }
}
