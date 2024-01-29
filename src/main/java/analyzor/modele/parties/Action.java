package analyzor.modele.parties;

import jakarta.persistence.*;

public class Action {
    private Move move;
    private float betSize;
    private float relativeBetSize;

    //constructeur
    public Action() {}
    public Action(Move move) {
        assert (move == Move.FOLD);
        this.move = move;
        this.betSize = 0;
    }

    public Action(Move move, float betSize) {
        this.move = move;
        this.betSize = betSize;
    }


    public float distance(Action autreAction) {
        //todo : est-ce utile ?
        return 0;
    }

    public boolean estFold() {
        return (this.move == Move.FOLD);
    }

    public float getBetSize() {
        return betSize;
    }

    public void setBetSize(float betSize) {
        this.betSize = betSize;
    }

    public void setMove(Move move) {
        this.move = move;
    }


    public void augmenterBet(float suppBet) {
        betSize += suppBet;
    }

    /**
     * important : procédure indispensable avant de persister l'objet
     * @param montantPot
     */
    public void setPot(float montantPot) {
        this.relativeBetSize = getRelativeBetSize(betSize, montantPot);
    }

    public float getRelativeBetSize(float betSize, float montantPot) {
        return this.betSize / montantPot;
    }

    public float getRelativeBetSize() {
        return this.relativeBetSize;
    }

    public Move getMove() {
        return this.move;
    }
}
