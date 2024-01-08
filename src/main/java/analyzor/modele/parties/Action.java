package analyzor.modele.parties;

import jakarta.persistence.*;

@Entity
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Move move;
    private int betSize;
    private float relativeBetSize;

    //constructeur
    public Action() {}
    public Action(Move move) {
        assert (move == Move.FOLD);
        this.move = move;
        this.betSize = 0;
    }

    public Action(Move move, int betSize) {
        this.move = move;
        this.betSize = betSize;
    }


    @PrePersist
    protected void onCreate() {
        if (move == Move.FOLD) {
            // todo : on doit mettre null ou zéro???
            betSize = 0;
        }
    }

    public float distance(Action autreAction) {
        //todo : à coder, permet de comparer deux actions (= return this.id)
        return 0;
    }

    public boolean estFold() {
        return (this.move == Move.FOLD);
    }

    public int getBetSize() {
        return betSize;
    }

    public void setBetSize(int betSize) {
        this.betSize = betSize;
    }

    public void setMove(Move move) {
        this.move = move;
    }


    public void augmenterBet(int suppBet) {
        betSize += suppBet;
    }

    /**
     * important : procédure indispensable avant de persister l'objet
     * @param montantPot
     */
    public void setPot(int montantPot) {
        this.relativeBetSize = getRelativeBetSize(betSize, montantPot);
    }

    public float getRelativeBetSize(int betSize, int montantPot) {
        return (float) this.betSize / montantPot;
    }

    public float getRelativeBetSize() {
        return this.relativeBetSize;
    }

    public Move getMove() {
        return this.move;
    }
}
