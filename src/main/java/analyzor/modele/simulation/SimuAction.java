package analyzor.modele.simulation;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.poker.RangeSauvegardable;

public class SimuAction {
    private NoeudAbstrait noeudAbstrait;
    private RangeSauvegardable range;

    public SimuAction(NoeudAbstrait noeudAbstrait, RangeSauvegardable range) {
    }

    public String getNom() {
        //todo
        return null;
    }

    public float getBetSize() {
        //todo
        return 0f;
    }

    public int getIndex() {
        //todo
        return 0;
    }

    public boolean estFold() {
        return noeudAbstrait.getMove() == Move.FOLD;
    }

    public Long getIdNoeud() {
        return noeudAbstrait.toLong();
    }

    public RangeSauvegardable getRange() {
        return range;
    }
}
