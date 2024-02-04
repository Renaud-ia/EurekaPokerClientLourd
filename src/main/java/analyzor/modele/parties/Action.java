package analyzor.modele.parties;

import jakarta.persistence.*;

public class Action {
    private Move move;
    private float betSize;
    private float relativeBetSize;

    //constructeur
    public Action() {}
    public Action(Move move) {
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

    public Move getMove() {
        return this.move;
    }
}
