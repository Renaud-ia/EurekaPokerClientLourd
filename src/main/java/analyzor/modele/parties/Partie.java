package analyzor.modele.parties;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Partie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private Variante variante;

    private float buyIn;
    private String nomHero;
    private String nomPartie;

    @Column(nullable = true)
    private LocalDateTime dPlayed;

    private LocalDateTime dSaved;

    // Getters, setters, etc.

    @PrePersist
    protected void onCreate() {
        dSaved = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "partie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MainEnregistree> mainsEnregistrees = new ArrayList<>();

    //constructeurs
    public Partie() {}

    //getters, setters

    private long getId() {
        return id;
    }


    // recommandé de réécrire equals et hashCode quand relation réciproque
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Partie )) return false;
        return id != null && id.equals(((Partie) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

