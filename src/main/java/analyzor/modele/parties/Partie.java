package analyzor.modele.parties;

import jakarta.persistence.*;

@Entity
public class Partie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private Variante variante;

    // recommandé de réécrire equals et hashCode quand relation réciproque
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Partie )) return false;
        return id != null && id.equals(((Partie) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private long getId() {
        return id;
    }
}

