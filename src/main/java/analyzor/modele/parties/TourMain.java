package analyzor.modele.parties;

import analyzor.modele.poker.Board;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
public class TourMain {
    public enum Round {
        PREFLOP, FLOP, TURN, RIVER;

        public Round suivant() {
            int newIndex = (this.ordinal() + 1) % Round.values().length;
            return Round.values()[newIndex];
        }

        public long toInt() {
            return this.ordinal();
        }
    }

    @Id
    private Long id;

    private Integer board;
    private int nJoueursDebut;
    private Round nomTour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private MainEnregistree main;

    @OneToMany(mappedBy = "tourMain")
    private List<Entree> entrees = new ArrayList<>();

    // on supprime les gains sans action
    @OneToMany(mappedBy = "tourMain")
    private List<GainSansAction> gainsSansAction = new ArrayList<>();

    //constructeurs
    public TourMain() {}
    public TourMain(Round nomTour, MainEnregistree mainEnregistree, Board board, int nJoueursInitiaux) {
        this.nomTour = nomTour;
        this.main = mainEnregistree;
        if (board == null) {
            this.board = null;
        }
        else {
            this.board = board.asInt();
        }
        this.nJoueursDebut = nJoueursInitiaux;
        genererId();
    }

    private void genererId() {
        long idMain = main.getId();
        this.id = (idMain << 6) + nomTour.ordinal();
    }

    public Long getId() {
        return id;
    }

    public List<Entree> getEntrees() {
        return entrees;
    }


    // recommandé de réécrire equals et hashCode quand relation réciproque
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TourMain )) return false;
        return id != null && id.equals(((TourMain) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
