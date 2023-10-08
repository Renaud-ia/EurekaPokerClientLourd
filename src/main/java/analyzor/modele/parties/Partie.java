package analyzor.modele.parties;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Partie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer idParse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Variante variante;

    private float buyIn;
    private String nomHero;
    private String nomPartie;

    @Column(nullable = true)
    private LocalDateTime dPlayed;

    private LocalDateTime dSaved;

    public Partie(Integer idParse, float buyIn, String nomHero, String nomPartie, LocalDateTime dateTournoi) {
        this.idParse = idParse;
        this.buyIn = buyIn;
        this.nomHero = nomHero;
        this.nomPartie = nomPartie;
        this.dPlayed = dateTournoi;
    }

    // Getters, setters, etc.

    @PrePersist
    protected void onCreate() {
        dSaved = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "partie", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MainEnregistree> mainsEnregistrees = new ArrayList<>();

    //constructeurs
    public Partie() {}

    public Partie(Variante variante, Integer idParse, float buyIn, String nomHero, String nomPartie, LocalDateTime dateTournoi) {
        this.variante = variante;
        this.idParse = idParse;
        this.buyIn = buyIn;
        this.nomHero = nomHero;
        this.nomPartie = nomPartie;
        this.dPlayed = dateTournoi;
    }

    //getters, setters

    private long getId() {
        return id;
    }

    public String getNomHero() {
        return nomHero;
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

