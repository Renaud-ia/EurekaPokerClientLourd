package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class GainSansAction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Joueur joueur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private TourMain tourMain;

    private int sommeGagnee;

    //constructeurs
    public GainSansAction() {}

    //getters, setters

    private long getId() {
        return id;
    }


    // recommandé de réécrire equals et hashCode quand relation réciproque
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GainSansAction )) return false;
        return id != null && id.equals(((GainSansAction) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
