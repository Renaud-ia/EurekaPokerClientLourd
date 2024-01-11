package analyzor.modele.simulation;

import analyzor.modele.arbre.noeuds.NoeudSituation;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.poker.RangeIso;

import java.util.HashMap;
import java.util.List;

/**
 * stocke les informations sur la situation AVANT les actions
 * stack des joueurs, si les joueurs sont actifs etc.
 * ainsi que les actions possibles
 */
public class SimuSituation {
    // stocke la liste des situations gardées en mémoire par la table
    private JoueurSimulation joueur;
    private HashMap<JoueurSimulation, Float> stacks;
    private boolean actionFixee = false;
    private List<SimuAction> actions;
    private NoeudAbstrait noeudAbstrait;
    private NoeudSituation noeudSituation;
    private float pot;
    private float potBounty;

    public SimuSituation(NoeudAbstrait noeudSituation,
                         JoueurSimulation joueur,
                         HashMap<JoueurSimulation, Float> stacksApresBlindes,
                         HashMap<JoueurSimulation, Boolean> joueurActif,
                         float pot, float potBounty) {
        this.joueur = joueur;
    }

    // interface publique utilisée par le controleur

    public JoueurSimulation getJoueur() {
        return joueur;
    }

    public float getStack() {
        return stacks.get(joueur);
    }

    public List<SimuAction> getActions() {
        // todo important on veut l'ordre suivant : fold, call, raise par ordre de bet size, all-in
        return actions;
    }

    // interface package-private

    NoeudAbstrait getNoeudAbstrait() {
        return noeudAbstrait;
    }

    float getPot() {
        return pot;
    }

    float getPotBounty() {
        return potBounty;
    }

    void deselectionnerAction() {
    }

    void fixerAction(int indexAction) {
    }

    boolean actionFixee() {
        return false;
    }

    int fixerActionParDefaut() {
        return 0;
    }

    SimuAction getActionSelectionnee() {
        return null;
    }

    SimuAction getAction(Integer indexAction) {
        return null;
    }

    public SimuAction getActionActuelle() {
        return null;
    }

    public HashMap<JoueurSimulation, Float> getStacks() {
        return stacks;
    }

    public HashMap<JoueurSimulation, Boolean> getJoueurFolde() {
        return null;
    }

    public NoeudSituation getNoeudSituation() {
        return noeudSituation;
    }

    public void ajouterAction(SimuAction simuAction) {
    }
}
