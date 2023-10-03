package analyzor.modele.parties;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Variante {
    public enum PokerRoom {
        WINAMAX, BETCLIC, POKERSTARS
    }

    public enum PokerFormat {
        SPIN, CASH_GAME, MTT
    }

    public enum Vitesse {
        NITRO, ULTRA_TURBO, TURBO, SEMI_TURBO, NORMALE
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

    private int startingStack;
    @Min(2)
    @Max(12)
    private int nPlayers;

    private int ante;

    private boolean ko;

    @OneToMany(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Partie> parties = new ArrayList<>();

    //constructeurs
    public Variante() {}

    //getters, setters, ...
    public List<Partie> getParties() {
        return parties;
    }

}
