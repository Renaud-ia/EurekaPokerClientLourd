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
    private int stackDepart;
    // nombre de joueurs est ici plutôt que dans variante, car dans winamax c'est à la fin qu'on le remplit
    private int nPlayers;

    @PrePersist
    protected void onCreate() {
        dSaved = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "partie")
    private List<MainEnregistree> mainsEnregistrees = new ArrayList<>();

    //constructeurs
    public Partie() {}

    public Partie(Variante variante,
                  Integer idParse,
                  float buyIn,
                  String nomHero,
                  String nomPartie,
                  LocalDateTime dateTournoi) {

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

    public LocalDateTime getDate() {
        return dPlayed;
    }

    public void setBuyIn(int buyIn) {
        this.buyIn = buyIn;
    }

    public Variante getVariante() {
        return this.variante;
    }

    public void setStackDepart(int stackDepart) {
        this.stackDepart = stackDepart;
    }

    public void setNombreJoueurs(int nombreJoueurs) {
        this.nPlayers = nombreJoueurs;
    }
}

