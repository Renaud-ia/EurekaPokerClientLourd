package analyzor.vue.donnees.format;

import analyzor.modele.parties.Variante;

import java.time.LocalDate;
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
    private LocalDateTime joueApres;
    private LocalDateTime joueAvant;
    private int nombrePartiesCalculees;
    private int nSituations;
    private int nSituationsResolues;

    // attributs modifiables
    private Long idBDD;
    private String nomFormat;
    private int nombrePartiesTotal;
    private boolean preflopCalcule;
    private boolean flopCalcule;
    private float pctAvancement;

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

        this.joueApres = LocalDateTime.of(1950, 1, 1, 0, 0);
        this.joueAvant = LocalDateTime.now();
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
            LocalDateTime joueAvant,
            LocalDateTime joueApres,
            int nSituations,
            int nSituationsResolues,
            int nombreParties,
            int nombrePartiesCalculees,
            boolean preflopCalcule,
            boolean flopCalcule,
            float pctAvancement
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

        this.joueAvant = joueAvant;
        this.joueApres = joueApres;

        this.nombrePartiesTotal = nombreParties;
        this.nombrePartiesCalculees = nombrePartiesCalculees;
        this.nSituations = nSituations;
        this.nSituationsResolues = nSituationsResolues;
        this.preflopCalcule = preflopCalcule;
        this.flopCalcule = flopCalcule;
        this.pctAvancement = pctAvancement;
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
            this.nSituationsResolues = 0;
        }

        public LocalDateTime getDateCreation() {
            return dateCreation;
        }

    public String getStatut() {
        if (nombrePartiesTotal == 0) {
            return "Aucune partie correspondante";
        }
        else if (nSituationsResolues > 0) {
            return "Calcul\u00E9 \u00E0 " + Math.round(pctAvancement * 100) + "% sur " + nombrePartiesCalculees + " parties";
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
        if (pokerFormat == Variante.PokerFormat.SPIN) {
            return nSituationsResolues >= 2;
        }
        return nSituationsResolues >= 1;
    }

    // actualisation après calcul

    public void setNombrePartiesCalculees(int nombresPartiesCalculees) {
        this.nombrePartiesCalculees = nombresPartiesCalculees;
    }

    public void setNombreSituations(int nombreSituations) {
        this.nSituations = nombreSituations;
    }

    public void setNombreSituationsCalculees(int nombreSituationsResolues) {
        this.nSituationsResolues = nombreSituationsResolues;
    }

    public void setPreflopCalcule(boolean preflopCalcule) {
        this.preflopCalcule = preflopCalcule;
    }

    public void setFlopCalcule(boolean flopCalcule) {
        this.flopCalcule = flopCalcule;
    }

    public int getNombreSituationsResolues() {
        return nSituationsResolues;
    }

    public int getNombreSituationsCalcul() {
        return nSituations;
    }

    public void setPctAvancement(float pctAvancement) {
        this.pctAvancement = pctAvancement;
    }

    public LocalDateTime getJoueAvant() {
        return joueAvant;
    }

    public LocalDateTime getJoueApres() {
        return joueApres;
    }

    public void setDateMinimum(LocalDate localDate) {
        this.joueApres = localDate.atStartOfDay();
    }

    public void setDateMaximum(LocalDate localDate) {
        this.joueAvant = localDate.atStartOfDay();
    }
}
