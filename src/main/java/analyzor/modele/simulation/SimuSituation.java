package analyzor.modele.simulation;

import analyzor.modele.arbre.noeuds.NoeudSituation;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.*;

/**
 * stocke les informations sur la situation AVANT les actions
 * stack des joueurs, si les joueurs sont actifs etc.
 * ainsi que les actions possibles
 */
public class SimuSituation {
    // stocke la liste des situations gardées en mémoire par la table
    private final NoeudSituation noeudSituation;
    private final TablePoker.JoueurTable joueur;
    private final HashMap<TablePoker.JoueurTable, Float> stacks;
    private final HashMap<TablePoker.JoueurTable, Boolean> joueurFolde;
    private final float pot;
    private final float potBounty;
    private final PriorityQueue<SimuAction> queueActions;
    private LinkedList<SimuAction> actionsTriees;
    private Integer actionSelectionnee;
    private final float dernierBet;

    public SimuSituation(NoeudSituation noeudSituation,
                         TablePoker.JoueurTable joueur,
                         HashMap<TablePoker.JoueurTable, Float> stacks,
                         HashMap<TablePoker.JoueurTable, Boolean> joueurFolde,
                         float pot, float potBounty, float dernierBet) {
        this.noeudSituation = noeudSituation;
        this.joueur = joueur;
        this.stacks = stacks;
        this.joueurFolde = joueurFolde;
        this.pot = pot;
        this.potBounty = potBounty;
        this.dernierBet = dernierBet;

        queueActions = new PriorityQueue<>(Comparator.comparingInt(SimuAction::ordreClassement));
    }

    // interface publique utilisée par le controleur

    public TablePoker.JoueurTable getJoueur() {
        return joueur;
    }

    public float getStack() {
        return stacks.get(joueur);
    }

    public LinkedList<SimuAction> getActions() {
        construireListeActions();
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
        if (actionFixee() != null) return null;
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

    Integer actionFixee() {
        return actionSelectionnee;
    }

    SimuAction getActionActuelle() {
        construireListeActions();
        return actionsTriees.get(actionSelectionnee);
    }

    HashMap<TablePoker.JoueurTable, Float> getStacks() {
        return stacks;
    }

    HashMap<TablePoker.JoueurTable, Boolean> getJoueurFolde() {
        return joueurFolde;
    }

    NoeudSituation getNoeudSituation() {
        return noeudSituation;
    }

    public void deselectionnerAction() {
        actionSelectionnee = null;
    }

    private void construireListeActions() {
        if (actionsTriees == null) {
            actionsTriees = new LinkedList<>();
            while (!(queueActions.isEmpty())) {
                actionsTriees.add(queueActions.poll());
            }
        }
        for (int i = 0; i < actionsTriees.size(); i++) {
            SimuAction action = actionsTriees.get(i);
            action.setIndex(i);
        }


    }

    public float getDernierBet() {
        return dernierBet;
    }
}
