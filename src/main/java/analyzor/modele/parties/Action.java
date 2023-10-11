package analyzor.modele.parties;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Action {




    public enum Move {
        FOLD, CHECK, CALL, RAISE, ALL_IN, CHECK_RAISE, RAISE_CALL
    }
    @Id
    private Long id;

    private Move move;
    private int betSize;
    //private float relativeBetSize;

    @OneToMany(mappedBy = "action")
    private List<Entree> entrees = new ArrayList<>();

    //constructeur
    public Action() {}
    public Action(Move move) {
        assert (move == Move.FOLD || move == Move.CHECK);
        this.move = move;
        this.betSize = 0;
        genererId();
    }

    public Action(Move move, int betSize) {
        this.move = move;
        this.betSize = betSize;
        genererId();
    }

    /* bug débile
    private void genererId() {
        this.id = ((long) ((int) (relativeBetSize * 100)) << 6) + move.ordinal();
        System.out.println("Id généré");
    }

     */

    private void genererId() {
        //27bits = 100.000.000 max
        this.id = ((long) move.ordinal() << 27) + betSize;
    }


    @PrePersist
    protected void onCreate() {
        if (move == Move.FOLD || move == Move.CHECK) {
            // todo : on doit mettre null ou zéro???
            betSize = 0;
        }
    }

    public float distance(Action autreAction) {
        //todo : à coder, permet de comparer deux actions
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


    /*
    public void setRelativeBetSize(float betSize) {
        this.relativeBetSize = betSize;
        genererId();
    }
     */

    public void augmenterBet(int suppBet) {
        betSize += suppBet;
    }

    public List<Entree> getEntrees() {
        return entrees;
    }

}
