package analyzor.vue.donnees;

import analyzor.modele.parties.Move;
import analyzor.modele.simulation.SimuSituation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DTOSituation {
    private final SimuSituation situation;
    private final LinkedList<InfosAction> actionsPossibles;
    private final DTOJoueur joueur;
    private final float stack;
    private Integer actionSelectionnee;

    public DTOSituation(SimuSituation situation, DTOJoueur joueur, float stack) {
        if (situation == null) throw new IllegalArgumentException("SITUATION EST NULL");
        actionsPossibles = new LinkedList<>();
        this.situation = situation;
        this.joueur = joueur;
        this.stack = stack;
    }

    public void ajouterAction(Move move, float betSize, int indexActionModele) {
        InfosAction nouvelleAction = new InfosAction(move.toString(), betSize, indexActionModele);
        actionsPossibles.add(nouvelleAction);
    }

    public SimuSituation getSituationModele() {
        return situation;
    }

    public void setActionSelectionnee(Integer indexAction) {
        actionSelectionnee = indexAction;
    }

    public void deselectionnerAction() {
        actionSelectionnee = null;
    }

    public String getNom() {
        return joueur.getNom() + " (" + stack + ")";
    }

    public LinkedList<InfosAction> getActions() {
        return actionsPossibles;
    }
}
