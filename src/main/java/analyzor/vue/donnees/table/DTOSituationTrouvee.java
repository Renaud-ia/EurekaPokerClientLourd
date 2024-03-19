package analyzor.vue.donnees.table;

import analyzor.modele.parties.Move;
import analyzor.modele.simulation.SimuSituation;

import java.util.LinkedList;

public class DTOSituationTrouvee implements DTOSituation {
    private final SimuSituation situation;
    private final LinkedList<InfosAction> actionsPossibles;
    private final DTOJoueur joueur;
    private final float stack;
    private Integer actionSelectionnee;

    public DTOSituationTrouvee(SimuSituation situation, DTOJoueur joueur, float stack) {
        if (situation == null) throw new IllegalArgumentException("SITUATION EST NULL");
        actionsPossibles = new LinkedList<>();
        this.situation = situation;
        this.joueur = joueur;
        this.stack = stack;
    }

    public void ajouterAction(Move move, float betSize, int indexActionModele) {
        InfosAction nouvelleAction = new InfosAction(move, betSize, indexActionModele);
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
        StringBuilder builderNom = new StringBuilder();
        builderNom.append(joueur.getNom()).append(" (");
        if (stack == (int) stack) {
            builderNom.append(((int) stack));
        }
        else builderNom.append(stack);
        builderNom.append("bb)");

        if (joueur.getHero()) builderNom.append(" (hero)");

        return builderNom.toString();
    }

    public LinkedList<InfosAction> getActions() {
        return actionsPossibles;
    }
}
