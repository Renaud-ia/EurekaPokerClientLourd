package analyzor.modele.simulation;

import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.poker.RangeSauvegardable;

public class SimuAction {
    private int index;
    private final NoeudAbstrait noeudAbstrait;
    private final RangeSauvegardable range;
    private final float betSize;

    

    SimuAction(NoeudAbstrait noeudAbstrait, RangeSauvegardable range, float betSize) {
        this.noeudAbstrait = noeudAbstrait;
        this.range = range;
        this.betSize = betSize;
    }

    void setIndex(int index) {
        
        this.index = index;
    }

    

    public int getIndex() {
        return index;
    }

    public float getBetSize() {
        return (float) Math.round(betSize * 10) / 10;
    }

    public RangeSauvegardable getRange() {
        return range;
    }


    

    Long getIdNoeud() {
        return noeudAbstrait.toLong();
    }

    boolean estFold() {
        return noeudAbstrait.getMove() == Move.FOLD;
    }

    

    public int ordreClassement() {
        return (int) ((noeudAbstrait.getMove().ordinal() << 18) + (getBetSize() * 10));
    }

    public Move getMove() {
        return noeudAbstrait.getMove();
    }

    public String toString() {
        return noeudAbstrait.getMove() + " " + getBetSize() + ", index : " + index;
    }

    public boolean isLeaf() {
        return noeudAbstrait.isLeaf();
    }
}
