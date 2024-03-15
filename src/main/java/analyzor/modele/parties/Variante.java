package analyzor.modele.parties;

import analyzor.modele.extraction.exceptions.InformationsIncorrectes;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Variante {


    public enum VariantePoker {
        HOLDEM_NO_LIMIT, OMAHA, INCONNU
    }

    public enum PokerFormat {
        SPIN, CASH_GAME, MTT, INCONNU
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private VariantePoker variantePoker;
    @Enumerated(EnumType.STRING)
    private PokerFormat format;
    private float buyIn;
    private int nombreJoueurs;
    private float ante;
    private float rake;
    private boolean ko;

    //constructeurs
    public Variante() {}

    /**
     * constructeur de la variante
     * @param buyIn pour cash-game = montant BB
     * @param ante valeur de l'ante en % bb,
     * @param rake ou rake en %pot si CG
     */
    public Variante(VariantePoker variantePoker,
                    PokerFormat pokerFormat,
                    float buyIn,
                    int nombreJoueurs,
                    float ante,
                    float rake,
                    boolean ko) {

        this.variantePoker = variantePoker;
        this.format = pokerFormat;
        this.buyIn = buyIn;
        this.nombreJoueurs = nombreJoueurs;
        this.ante = ante;
        this.rake = rake;
        this.ko = ko;
    }

    public boolean hasBounty() {
        return ko;
    }

    public int getNombreJoueurs() {
        return nombreJoueurs;
    }

    public float getBuyIn() {
        return buyIn;
    }
}
