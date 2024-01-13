package analyzor.modele.simulation;

import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.poker.RangeSauvegardable;

public class SimuAction implements Comparable<SimuAction> {
    private int index;
    private final NoeudAbstrait noeudAbstrait;
    private final RangeSauvegardable range;
    private final float betSize;

    // construction de l'action

    SimuAction(NoeudAbstrait noeudAbstrait, RangeSauvegardable range, float betSize) {
        this.noeudAbstrait = noeudAbstrait;
        this.range = range;
        this.betSize = betSize;
    }

    void setIndex(int index) {
        // todo : trouver une meilleure manière de gérer les index des actions (directement en appelant SimuSituation??)
        this.index = index;
    }

    // méthodes publiques pour récupérer les infos

    public int getIndex() {
        return index;
    }

    public String getNom() {
        StringBuilder nomAction = new StringBuilder();
        nomAction.append(noeudAbstrait.getMove().toString());
        if (getBetSize() > 0) nomAction.append(" ").append(getBetSize());

        return nomAction.toString();
    }

    public float getBetSize() {
        // todo on voudrait des BetSize "smooth" : 2 / 2.1 / 2.2 / 2.5 / 3.5 ou sinon un entier
        return (float) Math.round(betSize * 10) / 10;
    }

    public RangeSauvegardable getRange() {
        return range;
    }

    @Override
    public int compareTo(SimuAction o) {
        return this.ordreClassement() - o.ordreClassement();
    }


    // méthodes package-private pour récupérer les infos

    Long getIdNoeud() {
        return noeudAbstrait.toLong();
    }

    boolean estFold() {
        return noeudAbstrait.getMove() == Move.FOLD;
    }

    // méthodes privées

    private int ordreClassement() {
        return (int) ((noeudAbstrait.getMove().ordinal() << 10) + (getBetSize() * 100));
    }
}
