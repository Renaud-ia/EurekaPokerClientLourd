package analyzor.modele.parties;

import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="situation_type",
        discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("SITUATION")
public class Situation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic
    private long id;
    private int rang;
    private int nJoueursActifs;
    private int tour;
    private int position;

    // si query est null, hibernate a besoin d'un constructeur vide
    public Situation() {}

    public Situation(int rang, int nJoueursActifs, int tour, int position) {
        this.rang = rang;
        this.nJoueursActifs = nJoueursActifs;
        this.tour = tour;
        this.position = position;
    }

    // Getters et Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getTour() {
        return tour;
    }

    public void setTour(int tour) {
        this.tour = tour;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public static void main(String[] args) {
        Situation situation = new Situation(0, 3, 1, 22);
        Situation situationTrouvee = (Situation) RequetesBDD.getOrCreate(situation, true);
    }


}
