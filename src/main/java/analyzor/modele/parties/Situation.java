package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="situation_type",
        discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("SITUATION")
public class Situation {
    @Id
    @Basic
    private Long id;
    private Integer rang;
    private Integer nJoueursActifs;
    private TourMain.Round tour;
    private Integer position;

    @OneToMany(mappedBy = "situation")
    private List<Entree> entrees = new ArrayList<>();

    //Constructeurs

    // si query est null, hibernate a besoin d'un constructeur vide
    public Situation() {}

    public Situation(int rang, int nJoueursActifs, TourMain.Round tour, int position) {
        this.rang = rang;
        this.nJoueursActifs = nJoueursActifs;
        this.tour = tour;
        this.position = position;
        genererId();
    }

    private void genererId() {
        this.id = (long) (rang << 24 |
                        nJoueursActifs << 18 |
                        tour.ordinal() << 12 |
                        position);
    }

    // Getters et Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Entree> getEntrees() {
        return entrees;
    }

    public int getRang() {
        return rang;
    }

    public void setRang(int rang) {
        this.rang = rang;
    }

    public int getNJoueursActifs() {
        return nJoueursActifs;
    }

    public void setNJoueursActifs(int nJoueursActifs) {
        this.nJoueursActifs = nJoueursActifs;
    }

    public TourMain.Round getTour() {
        return tour;
    }

    public void setTour(TourMain.Round tour) {
        this.tour = tour;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isRound(TourMain.Round round) {
        return this.tour == round;
    }
}
