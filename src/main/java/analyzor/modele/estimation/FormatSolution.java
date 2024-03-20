package analyzor.modele.estimation;

import analyzor.modele.arbre.noeuds.NoeudSituation;
import analyzor.modele.parties.*;
import jakarta.persistence.*;
import org.apache.commons.collections4.Bag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// doit être classe publique pour compatibilité avec Hibernate
@Entity
public class FormatSolution {
    @Transient
    private final static float PAS_STANDARD = 5f;
    // todo : rajouter toutes les options possibles pour ne pas avoir à modifier par la suite
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomPersonnaliseFormat;
    private LocalDateTime dSaved;
    private Variante.VariantePoker variantePoker;
    private Variante.PokerFormat pokerFormat;
    private PokerRoom room;
    private float anteMin;
    private float anteMax;
    private float rakeMin;
    private float rakeMax;
    private boolean ko;
    //on laisse la possibilité que soit nul
    private Integer nJoueurs;
    private float minBuyIn;
    private float maxBuyIn;

    //TODO : on pourrait rajouter d'autres choses (date, vitesse, starting stack mais on commence simple)
    private LocalDateTime joueApres;
    private LocalDateTime joueAvant;
    private Float heureMin;
    private Float heureMax;
    private float pasResolution;

    // état de la résolution du format
    private int nSituations;
    private int nSituationsResolues;
    private int nombreParties;
    private int nombresPartiesCalculees;
    private boolean preflopCalcule;
    private boolean flopCalcule;
    private boolean turnCalcule;
    private boolean riverCalcule;

    @PrePersist
    protected void onCreate() {
        dSaved = LocalDateTime.now();
    }

    //constructeurs

    public FormatSolution() {};

    // constructeur simple
    public FormatSolution(String nomPersonnaliseFormat,
                          Variante.PokerFormat pokerFormat,
                          float anteMin,
                          float anteMax,
                          float rakeMin,
                          float rakeMax,
                          boolean ko,
                          int nJoueurs,
                          float minBuyIn,
                          float maxBuyIn) {
        this.nomPersonnaliseFormat = nomPersonnaliseFormat;
        this.variantePoker = null;
        this.pokerFormat = pokerFormat;
        this.anteMin = anteMin;
        this.anteMax = anteMax;
        this.rakeMin = rakeMin;
        this.rakeMax = rakeMax;

        this.ko = ko;
        this.nJoueurs = nJoueurs;

        this.minBuyIn = minBuyIn;
        this.maxBuyIn = maxBuyIn;

        this.pasResolution = PAS_STANDARD;

        this.room = null;
        this.joueAvant = null;
        this.joueApres = null;
        this.heureMin = null;
        this.heureMax = null;

        this.nSituations = 0;
        this.nSituationsResolues = 0;
        this.nombresPartiesCalculees = 0;
        this.nombreParties = 0;

        this.preflopCalcule = false;
        this.flopCalcule = false;
        this.turnCalcule = false;
        this.riverCalcule = false;
    }

    // getters infos format

    public Long getId() {
        return id;
    }

    public int getNombreJoueurs() {
        int MAX_JOUEURS = 10;
        if (nJoueurs == null) {
            return MAX_JOUEURS;
        }
        else { return nJoueurs; }
    }

    public String getNomFormat() {
        return nomPersonnaliseFormat;
    }

    public float getAnteMin() {
        return anteMin;
    }

    public float getAnteMax() {
        return anteMax;
    }

    public boolean getKO() {
        return ko;
    }

    public float getMinBuyIn() {
        return minBuyIn;
    }

    public float getMaxBuyIn() {
        return maxBuyIn;
    }

    public Variante.PokerFormat getPokerFormat() {
        return pokerFormat;
    }

    public float getRakeMin() {
        return rakeMin;
    }

    public float getRakeMax() {
        return rakeMax;
    }

    // modifications de l'état du format

    public void setCalcule(TourMain.Round round) {
        nombresPartiesCalculees = nombreParties;

        if (round == TourMain.Round.PREFLOP) {
            preflopCalcule = true;
        }
        else if (round == TourMain.Round.FLOP) {
            flopCalcule = true;
        }
        else if (round == TourMain.Round.TURN) {
            turnCalcule = true;
        }
        else if (round == TourMain.Round.RIVER) {
            riverCalcule = true;
        }

        else throw new IllegalArgumentException("Round inconnu : " + round);
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

    public void setNombreParties(int count) {
        this.nombreParties = count;
    }

    public int getNombreParties() {
        return nombreParties;
    }

    public int getNombresPartiesCalculees() {
        return nombresPartiesCalculees;
    }

    public boolean getPreflopCalcule() {
        return preflopCalcule;
    }

    public boolean getFlopCalcule() {
        return flopCalcule;
    }

    @Override
    public String toString() {
        return pokerFormat.toString() + " " + nJoueurs + "j." + "[" + minBuyIn + "-" + maxBuyIn + "]";
    }


    // appelé quand on reset le format
    public void setNonCalcule() {
        this.preflopCalcule = false;
        this.nombresPartiesCalculees = 0;
        this.nSituations = 0;
        this.nSituationsResolues = 0;
    }

    public LocalDateTime getDateCreation() {
        return dSaved;
    }

    public void changerNom(String nouveauNom) {
        this.nomPersonnaliseFormat = nouveauNom;
    }

    public int getNombreSituations() {
        return nSituations;
    }

    public int getNombreSituationsResolues() {
        return nSituationsResolues;
    }

    public void setNombreSituations(int size) {
        this.nSituations = size;
    }

    public void setNombreSituationsResolues(int nombreSituationsResolues) {
        this.nSituationsResolues = nombreSituationsResolues;
    }

    public void setNombrePartiesCalculees(int nombreParties) {
        this.nombresPartiesCalculees = nombreParties;
    }
}
