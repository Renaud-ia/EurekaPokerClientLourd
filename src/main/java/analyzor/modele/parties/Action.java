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
    private Integer betSize;

    @OneToMany(mappedBy = "action")
    private List<Entree> entrees = new ArrayList<>();

    //constructeur
    public Action() {}
    public Action(Move move) {
        assert (move == Move.FOLD || move == Move.CHECK);
        this.move = move;
        this.betSize = 0;
        generererId();
    }

    public Action(Move move, int betSize) {
        this.move = move;
        this.betSize = betSize;
        generererId();
    }

    private void generererId() {
        this.id = ((long) move.ordinal() << 6) + betSize;
    }


    @PrePersist
    protected void onCreate() {
        if (move == Move.FOLD || move == Move.CHECK) {
            // todo : on doit mettre null ou zéro???
            betSize = null;
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
        if (betSize == null) return 0;
        else return betSize;
    }

    public void augmenterBet(int suppBet) {
        betSize += suppBet;
    }

    public List<Entree> getEntrees() {
        return entrees;
    }

}
