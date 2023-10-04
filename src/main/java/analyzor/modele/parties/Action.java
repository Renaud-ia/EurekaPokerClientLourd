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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Move move;
    private Integer betSize;

    //constructeur
    public Action() {}

    @OneToMany(mappedBy = "action")
    private List<Entree> actionReelles = new ArrayList<>();

    @OneToMany(mappedBy = "actionIso")
    private List<Entree> actionIso = new ArrayList<>();

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
        return betSize;
    }

    public void augmenterBet(int suppBet) {
        betSize += suppBet;
    }

}
