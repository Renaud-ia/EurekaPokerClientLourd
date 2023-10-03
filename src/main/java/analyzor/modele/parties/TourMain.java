package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class TourMain {
    public enum Round {
        PREFLOP, FLOP, TURN, RIVER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int board;
    private int nJoueursDebut;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private MainEnregistree main;

    public Long getId() {
        return id;
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
