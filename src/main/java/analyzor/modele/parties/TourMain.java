package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private MainEnregistree main;

    @OneToMany(mappedBy = "tourMain")
    private List<Entree> entrees = new ArrayList<>();

    // on supprime les gains sans action
    @OneToMany(mappedBy = "tourMain", cascade = CascadeType.REMOVE)
    private List<GainSansAction> gainsSansAction = new ArrayList<>();

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
