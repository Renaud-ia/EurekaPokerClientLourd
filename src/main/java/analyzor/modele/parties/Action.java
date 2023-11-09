package analyzor.modele.parties;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Action {
    @Id
    private Long id;

    private Move move;
    private int betSize;
    private float relativeBetSize;

    @OneToMany(mappedBy = "action")
    private List<Entree> entrees = new ArrayList<>();

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

    private void genererId() {
        // 15 bits = 300x le pot max
        this.id = ((long) ((int) (relativeBetSize * 100)) << 15) + move.ordinal();
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
        genererId();
    }

    // on va pré discrétiser le BetSize si on est sur RAISE/ALL_IN
    private float getRelativeBetSize(int betSize, int montantPot) {
        //todo : discrétiser le BetSize
        return (float) this.betSize / montantPot;
    }

    public List<Entree> getEntrees() {
        return entrees;
    }

}
