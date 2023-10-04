package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class MainEnregistree {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private long idNonUnique;

    // todo int ou string???
    private int heroCombo;
    private int montantBB;
    private boolean showdown;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Partie partie;

    @OneToMany(mappedBy = "main", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TourMain> toursMain = new ArrayList<>();

    //constructeurs
    public MainEnregistree() {}

    public MainEnregistree(int idNonUnique, int montantBB, Partie partie) {
        this.idNonUnique = idNonUnique;
        this.montantBB = montantBB;
        this.partie = partie;
    }

    //getters, setters...

    private Long getId() {
        return id;
    }


    // recommandé de réécrire equals et hashCode quand relation réciproque
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MainEnregistree)) return false;
        return id != null && id.equals(((MainEnregistree) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void setCartesHero(int intCartesHero) {
        heroCombo = intCartesHero;
    }

    public void setShowdown(boolean showdown) {
        this.showdown = showdown;
    }
}