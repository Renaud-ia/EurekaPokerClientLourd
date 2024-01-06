package analyzor.vue.donnees;

import java.util.ArrayList;
import java.util.List;

public class DTOSituation {
    private final int indexModele;
    private final List<InfosAction> actionsPossibles;
    private final DTOJoueur joueur;
    private final float stack;

    public DTOSituation(int indexModele, DTOJoueur joueur, float stack) {
        actionsPossibles = new ArrayList<>();
        this.indexModele = indexModele;
        this.joueur = joueur;
        this.stack = stack;
    }

    public void ajouterAction(String nomAction, float betSize, int indexActionModele) {
        InfosAction nouvelleAction = new InfosAction(nomAction, betSize, indexActionModele);
        actionsPossibles.add(nouvelleAction);
    }

    public int getIndexModele() {
        return indexModele;
    }

    public void setActionSelectionnee(Integer indexAction) {
    }
}
