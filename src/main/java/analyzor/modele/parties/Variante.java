package analyzor.modele.parties;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Variante {

    public enum PokerFormat {
        SPIN, CASH_GAME, MTT, INCONNU
    }

    public enum Vitesse {
        NITRO, ULTRA_TURBO, TURBO, SEMI_TURBO, NORMALE, INCONNU
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Enumerated(EnumType.STRING)
    private PokerRoom room;

    @Enumerated(EnumType.STRING)
    private PokerFormat format;

    @Enumerated(EnumType.STRING)
    private Vitesse vitesse;

    private float ante;

    private boolean ko;

    //todo : il faut déterminer ça après l'enregistrement des mains en regardant la 1ère main
    private int startingStack;
    private int nPlayers;


    @OneToMany(mappedBy = "variante", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Partie> parties = new ArrayList<>();

    //constructeurs
    public Variante() {}

    public Variante(PokerRoom room, PokerFormat pokerFormat, Vitesse vitesse, float ante, boolean ko) {
        this.room = room;
        this.format = pokerFormat;
        this.vitesse = vitesse;
        this.ante = ante;
        this.ko = ko;
        this.startingStack = 0;
        this.nPlayers = 0;
    }

    //getters, setters, ...
    public List<Partie> getParties() {
        return parties;
    }

    public int getId() {
        return (int) id;
    }

}
