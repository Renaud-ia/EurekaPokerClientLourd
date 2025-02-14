package analyzor.modele.parties;

import analyzor.modele.estimation.FormatSolution;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
public class Partie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PokerRoom room;

    private Long idParse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Variante variante;
    private String nomHero;
    private String nomPartie;

    @Column(nullable = true)
    private LocalDateTime dPlayed;

    private LocalDateTime dSaved;
    // nombre de joueurs est ici plutôt que dans variante, car dans winamax c'est à la fin qu'on le remplit

    @PrePersist
    protected void onCreate() {
        dSaved = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "partie")
    private List<MainEnregistree> mainsEnregistrees = new ArrayList<>();

    //constructeurs
    public Partie() {}

    public Partie(Variante variante,
                  PokerRoom room,
                  Long idParse,
                  String nomHero,
                  String nomPartie,
                  LocalDateTime dateTournoi) {
        this.variante = variante;
        this.room = room;
        this.idParse = idParse;
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

    public List<MainEnregistree> getMains() {
        return mainsEnregistrees;
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

    public Variante getVariante() {
        return this.variante;
    }
}

