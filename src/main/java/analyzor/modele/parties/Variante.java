package analyzor.modele.parties;

import jakarta.persistence.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private PokerRoom room;

    @Enumerated(EnumType.STRING)
    private PokerFormat format;

    @Enumerated(EnumType.STRING)
    private Vitesse vitesse;

    private float ante;

    private boolean ko;

    //constructeurs
    public Variante() {}

    public Variante(PokerRoom room, PokerFormat pokerFormat, Vitesse vitesse, float ante, boolean ko) {
        this.room = room;
        this.format = pokerFormat;
        this.vitesse = vitesse;
        this.ante = ante;
        this.ko = ko;
    }
}
