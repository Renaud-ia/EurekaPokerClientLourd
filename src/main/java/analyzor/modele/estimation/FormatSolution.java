package analyzor.modele.estimation;

import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

// doit être classe publique pour compatibilité avec Hibernate
@Entity
public class FormatSolution {
    @Id
    @GeneratedValue()
    private Long id;

    private Variante.PokerFormat pokerFormat;

    private boolean ante;
    private boolean ko;
    //on laisse la possibilité que soit nul
    private Integer nJoueurs;
    private int minBuyIn;
    private int maxBuyIn;

    //TODO : on pourrait rajouter d'autres choses (date, vitesse, starting stack mais on commence simple)

    // état de la résolution du format
    private boolean preflopCalcule;
    private boolean flopCalcule;
    private boolean turnCalcule;
    private boolean riverCalcule;

    //constructeurs

    public FormatSolution() {};

    public FormatSolution(Variante.PokerFormat pokerFormat, boolean ante, Integer nJoueurs, int minBuyIn, int maxBuyIn) {
        this.pokerFormat = pokerFormat;
        this.ante = ante;
        this.nJoueurs = nJoueurs;
        this.minBuyIn = minBuyIn;
        this.maxBuyIn = maxBuyIn;

        this.preflopCalcule = false;
        this.flopCalcule = false;
        this.turnCalcule = false;
        this.riverCalcule = false;
    };

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
}
