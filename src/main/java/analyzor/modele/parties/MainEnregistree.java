package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class MainEnregistree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long idNonUnique;
    private int heroCombo;
    private float montantBB;

    private boolean showdown;


    private boolean tableFinale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Partie partie;

    @OneToMany(mappedBy = "main")
    private List<TourMain> toursMain = new ArrayList<>();


    public MainEnregistree() {}

    public MainEnregistree(long idNonUnique, float montantBB, Partie partie) {
        this.idNonUnique = idNonUnique;
        this.montantBB = montantBB;
        this.partie = partie;
    }





    public Long getId() {
        return id;
    }

    public List<TourMain> getTours() {
        return this.toursMain;
    }



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

    public boolean getShowdown() {
        return showdown;
    }

    public Partie getPartie() {
        return partie;
    }

    public int getComboHero() {
        return heroCombo;
    }

    public long getIdNonUnique() {
        return idNonUnique;
    }
}