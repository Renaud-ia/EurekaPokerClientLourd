package analyzor.vue.donnees;

import analyzor.modele.simulation.SimuSituation;

import java.util.ArrayList;
import java.util.List;

public class DTOSituation {
    private final SimuSituation situation;
    private final List<InfosAction> actionsPossibles;
    private final DTOJoueur joueur;
    private final float stack;

    public DTOSituation(SimuSituation situation, DTOJoueur joueur, float stack) {
        actionsPossibles = new ArrayList<>();
        this.situation = situation;
        this.joueur = joueur;
        this.stack = stack;
    }

    public void ajouterAction(String nomAction, float betSize, int indexActionModele) {
        InfosAction nouvelleAction = new InfosAction(nomAction, betSize, indexActionModele);
        actionsPossibles.add(nouvelleAction);
    }

    public SimuSituation getSituationModele() {
        return situation;
    }

    public void setActionSelectionnee(Integer indexAction) {
    }

    public void deselectionnerAction() {
    }
}
