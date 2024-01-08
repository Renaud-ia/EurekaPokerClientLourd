package analyzor.modele.parties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class DataRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private PokerRoom room;
    private int nombreMains;

    //constructeurs
    public DataRoom() {}

    public DataRoom(PokerRoom room) {
        this.room = room;
    }

    //getters setters
    public void addNombreMains(int nombreMains) {
        this.nombreMains += nombreMains;
    }
    public void setNombreMains(int nombreMains) {
        this.nombreMains = nombreMains;
    }

    public int getNombreMains() {
        return nombreMains;
    }
}
