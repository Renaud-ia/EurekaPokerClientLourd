package analyzor.vue.donnees.format;

import analyzor.modele.parties.Variante;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * interface entre controleur et vue pour affichage des données
 * stocke les informations sur les formats
 * bidirectionnel modèle->vue, vue->modèle
 * gère la mise en forme des données pour affichage
 */

public class DTOFormat {
    // attributs non modifiables
    private final LocalDateTime dateCreation;
    private final Variante.PokerFormat pokerFormat;
    private float anteMin;
    private float anteMax;
    private float rakeMin;
    private float rakeMax;
    private boolean ko;
    private int nJoueurs;
    private float minBuyIn;
    private float maxBuyIn;
    private int nombrePartiesCalculees;
    private int nSituations;
    private int nSituationsResolues;

    // attributs modifiables
    private Long idBDD;
    private String nomFormat;
    private int nombrePartiesTotal;
    private boolean preflopCalcule;
    private boolean flopCalcule;

    // constructeur pour affichage lors de création nouveau format
    DTOFormat(Variante.PokerFormat format, LocalDateTime dateCreation, int nJoueurs) {
        this.idBDD = null;
        this.dateCreation = dateCreation;
        this.nomFormat = "Nouveau format";
        this.pokerFormat = format;
        this.anteMin = 0f;
        this.anteMax = 0f;
        this.rakeMin = 0f;
        this.rakeMax = 0f;
        this.ko = false;
        this.nJoueurs = nJoueurs;
        this.minBuyIn = 0f;
        this.maxBuyIn = 0f;
    }

    public DTOFormat(
            Long idBDD,
            String nomFormat,
            LocalDateTime dateCreation,
            Variante.PokerFormat pokerFormat,
            float anteMin,
            float anteMax,
            float rakeMin,
            float rakeMax,
            boolean ko,
            int nJoueurs,
            float minBuyIn,
            float maxBuyIn,
            int nSituations,
            int nSituationsResolues,
            int nombreParties,
            int nombrePartiesCalculees,
            boolean preflopCalcule,
            boolean flopCalcule
        ) {

        this.idBDD = idBDD;
        this.dateCreation = dateCreation;
        this.nomFormat = nomFormat;
        this.pokerFormat = pokerFormat;
        this.anteMin = anteMin;
        this.anteMax = anteMax;
        this.rakeMin = rakeMin;
        this.rakeMax = rakeMax;
        this.ko = ko;
        this.nJoueurs = nJoueurs;
        this.minBuyIn = minBuyIn;
        this.maxBuyIn = maxBuyIn;
        this.nombrePartiesTotal = nombreParties;
        this.nombrePartiesCalculees = nombrePartiesCalculees;
        this.nSituations = nSituations;
        this.nSituationsResolues = nSituationsResolues;
        this.preflopCalcule = preflopCalcule;
        this.flopCalcule = flopCalcule;
    }


        // interface de consultation pour affichage et récupération données par contrôleur
        public Long getIdBDD() {
            return idBDD;
        }
        public Variante.PokerFormat getPokerFormat() {
            return pokerFormat;
        }

        public String getNomFormat() {
            return nomFormat;
        }

        public float getAnteMin() {
            return anteMin;
        }

        public float getAnteMax() {
            return anteMax;
        }

        public float getRakeMin() {
            return rakeMin;
        }

        public float getRakeMax() {
            return rakeMax;
        }

        public boolean getBounty() {
            return ko;
        }

        public int getnJoueurs() {
            return nJoueurs;
        }

        public float getMinBuyIn() {
            return minBuyIn;
        }

        public float getMaxBuyIn() {
            return maxBuyIn;
        }

        public boolean isPreflopCalcule() {
            return preflopCalcule;
        }

        public boolean isFlopCalcule() {
            return flopCalcule;
        }

        public int getNombreParties() {
            return nombrePartiesTotal;
        }

        public boolean selectionnable() {
            return preflopCalcule;
        }

        // ajout des infos lors de la création du format par controleur

        public void setNombreParties(int nombreParties) {
            this.nombrePartiesTotal = nombreParties;
        }

        public void setIdBDD(long idBDD) {
            this.idBDD = idBDD;
        }

        public void setNonCalcule() {
            this.preflopCalcule = false;
            this.nombrePartiesCalculees = 0;
        }

        public LocalDateTime getDateCreation() {
            return dateCreation;
        }

    public String getStatut() {
        if (nombrePartiesTotal == 0) {
            return "Aucune partie correspondante";
        }
        else if (preflopCalcule) {
            float pctCalcule = (float) nSituationsResolues / nSituations;
            return "Calcul\u00E9 \u00E0 " + Math.round(pctCalcule * 100) + " sur " + nombrePartiesCalculees + " parties";
        }

        else return "Non calcul\u00E9";
    }

    // méthodes package private pour fixer les valeurs lors de la création
    void setNom(String nouveauNom) {
        this.nomFormat = nouveauNom;
    }

    void setNombreJoueurs(int nJoueurs) {
        this.nJoueurs = nJoueurs;
    }

    public void setMinAnte(int valeurSlider) {
        this.anteMin = valeurSlider;
    }

    public void setMaxAnte(int valeurSlider) {
        this.anteMax = valeurSlider;
    }

    public void setMinRake(int valeurSlider) {
        this.rakeMin = valeurSlider;
    }

    public void setMaxRake(int valeurSlider) {
        this.rakeMax = valeurSlider;
    }

    public void setBounty(boolean etat) {
        this.ko = etat;
    }

    public void setMinBuyIn(int valeurSlider) {
        this.minBuyIn = valeurSlider;
    }

    public void setMaxBuyIn(int valeurSlider) {
        this.maxBuyIn = valeurSlider;
    }

    public boolean estConsultable() {
        return nSituationsResolues > 0;
    }
}
