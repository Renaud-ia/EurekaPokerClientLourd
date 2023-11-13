package analyzor.modele.estimation;

import analyzor.modele.parties.Partie;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

// doit être classe publique pour compatibilité avec Hibernate
@Entity
public class FormatSolution {
    @Id
    private Long id;

    private Variante.PokerFormat pokerFormat;

    private boolean ante;
    private boolean ko;
    //on laisse la possibilité que soit nul
    private Integer nJoueurs;
    private int minBuyIn;
    private int maxBuyIn;
    private int nombreParties;

    //TODO : on pourrait rajouter d'autres choses (date, vitesse, starting stack mais on commence simple)

    // état de la résolution du format
    private int nouvellesParties;
    private boolean preflopCalcule;
    private boolean flopCalcule;
    private boolean turnCalcule;
    private boolean riverCalcule;

    // parties répertoriées
    @ManyToMany
    @JoinTable(
            name = "format_solution_partie",
            joinColumns = @JoinColumn(name = "format_solution_id"),
            inverseJoinColumns = @JoinColumn(name = "partie_id")
    )
    private List<Partie> parties = new ArrayList<>();

    //constructeurs

    public FormatSolution() {};

    public FormatSolution(Variante.PokerFormat pokerFormat, boolean ante, boolean ko,
                          Integer nJoueurs, int minBuyIn, int maxBuyIn) {
        this.pokerFormat = pokerFormat;
        this.ante = ante;
        this.ko = ko;
        this.nJoueurs = nJoueurs;
        this.minBuyIn = minBuyIn;
        this.maxBuyIn = maxBuyIn;

        this.preflopCalcule = false;
        this.flopCalcule = false;
        this.turnCalcule = false;
        this.riverCalcule = false;

        this.id = ((long) pokerFormat.ordinal() << 34L) |
                ((long) (ante ? 1 : 0) << 32L) |
                ((long) (ko ? 1 : 0) << 30L) |
                ((long) nJoueurs << 24) |
                ((long) minBuyIn << 12) |
                maxBuyIn;
    }

    // todo pour test à supprimer
    public FormatSolution(Variante.PokerFormat pokerFormat, int nombreJoueurs) {
        this.pokerFormat = pokerFormat;
        this.nJoueurs = nombreJoueurs;
        this.ante = false;
        this.ko = false;
        this.minBuyIn = 0;
        this.maxBuyIn = 100;
    }

    public void setCalcule(TourMain.Round round) {
        if (round == TourMain.Round.PREFLOP) {
            this.preflopCalcule = true;
        }
        else if (round == TourMain.Round.FLOP) {
            this.flopCalcule = true;
        }
        else if (round == TourMain.Round.TURN) {
            this.turnCalcule = true;
        }
        else if (round == TourMain.Round.RIVER) {
            this.riverCalcule = false;
        }
    }

    public boolean estCalcule(TourMain.Round round) {
        if (round == TourMain.Round.PREFLOP) {
            return preflopCalcule;
        }
        else if (round == TourMain.Round.FLOP) {
            return flopCalcule;
        }
        else if (round == TourMain.Round.TURN) {
            return turnCalcule;
        }
        else if (round == TourMain.Round.RIVER) {
            return riverCalcule;
        }

        return false;
    }

    public int getNombreJoueurs() {
        int MAX_JOUEURS = 10;
        if (nJoueurs == null) {
            return MAX_JOUEURS;
        }
        else { return nJoueurs; }
    }

    protected List<Partie> getParties() {
        return parties;
    }

    public Variante.PokerFormat getNomFormat() {
        return pokerFormat;
    }

    public boolean getAnte() {
        return ante;
    }

    public boolean getKO() {
        return ko;
    }

    public int getMinBuyIn() {
        return minBuyIn;
    }

    public int getMaxBuyIn() {
        return maxBuyIn;
    }

    public void setNumberOfParties(int count) {
        this.nombreParties = count;
    }

    public Long getId() {
        return id;
    }

    public int getNombreParties() {
        return nombreParties;
    }

    public boolean getPreflopCalcule() {
        return preflopCalcule;
    }

    public boolean getFlopCalcule() {
        return flopCalcule;
    }

    public int getNouvellesParties() {
        return nouvellesParties;
    }
}
