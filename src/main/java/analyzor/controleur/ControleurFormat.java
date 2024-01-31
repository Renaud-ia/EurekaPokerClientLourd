package analyzor.controleur;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.GestionnaireFormat;
import analyzor.modele.parties.Variante;
import analyzor.vue.donnees.format.DTOFormat;
import analyzor.vue.gestionformat.FenetreFormat;
import analyzor.vue.FenetrePrincipale;

import java.util.List;

/**
 * interface de création des formats et de calcul
 */
public class ControleurFormat implements ControleurSecondaire {
    private final ControleurPrincipal controleurPrincipal;
    private final FenetreFormat vue;

    public ControleurFormat(FenetrePrincipale fenetrePrincipale, ControleurPrincipal controleur) {
        this.controleurPrincipal = controleur;
        this.vue = new FenetreFormat(fenetrePrincipale, this);
    }

    /**
     * démarrage du contrôleur, on va actualiser et sélectionner les formats à afficher
     */
    @Override
    public void demarrer() {
        List<FormatSolution> listFormats = GestionnaireFormat.formatsDisponibles();
        for (FormatSolution format : listFormats) {
            GestionnaireFormat.actualiserNombreParties(format);

            DTOFormat formatTrouve = new DTOFormat(
                    format.getId(),
                    format.getNomFormat(),
                    format.getDateCreation(),
                    format.getPokerFormat(),
                    format.getAnteMin(),
                    format.getAnteMax(),
                    format.getRakeMin(),
                    format.getRakeMax(),
                    format.getKO(),
                    format.getNombreJoueurs(),
                    format.getMinBuyIn(),
                    format.getMaxBuyIn(),
                    format.getNombreParties(),
                    format.getNombresPartiesCalculees(),
                    format.getPreflopCalcule(),
                    format.getFlopCalcule()
            );

            vue.ajouterFormat(formatTrouve);

        }
        this.vue.actualiser();

        lancerVue();
    }

    @Override
    public void lancerVue() {
        this.vue.setVisible(true);
    }

    @Override
    public void desactiverVue() {
        this.vue.setVisible(false);
    }

    public boolean creerFormat(
            DTOFormat infosFormat) {
        FormatSolution nouveauFormat = new FormatSolution(
                infosFormat.getNomFormat(),
                infosFormat.getPokerFormat(),
                infosFormat.getAnteMin(),
                infosFormat.getAnteMax(),
                infosFormat.getRakeMin(),
                infosFormat.getRakeMax(),
                infosFormat.getBounty(),
                infosFormat.getnJoueurs(),
                infosFormat.getMinBuyIn(),
                infosFormat.getMaxBuyIn());

        try {
            FormatSolution formatCree = GestionnaireFormat.ajouterFormat(nouveauFormat);

            if (formatCree == null) return false;

            Long idBDD = formatCree.getId();
            int nParties = formatCree.getNombreParties();

            infosFormat.setNombreParties(nParties);
            infosFormat.setIdBDD(idBDD);
            vue.ajouterFormat(infosFormat);
            this.vue.actualiser();

            System.out.println("FORMAT CRREE");

            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void formatSelectionne(DTOFormat infosFormat) {
        FormatSolution formatSolution = GestionnaireFormat.getFormatSolution(infosFormat.getIdBDD());
        controleurPrincipal.formatSelectionne(formatSolution);
        desactiverVue();
    }

    public boolean reinitialiser(DTOFormat formatModifie) {
        try {
            GestionnaireFormat.supprimerRanges(formatModifie.getIdBDD());
            formatModifie.setNonCalcule();
            this.vue.actualiser();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean supprimerFormat(DTOFormat infosFormat) {
        try {
            GestionnaireFormat.supprimerFormat(infosFormat.getIdBDD());
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean changerNomFormat(DTOFormat infosFormat, String nouveauNom) {
        try {
            GestionnaireFormat.changerNomFormat(infosFormat.getIdBDD(), nouveauNom);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    // controle du worker de calcul

    /**
     * appelé par la fenêtre de gestion des formats
     * @param formatCalcule le format à calculer qui contient idBDD
     * @return un worker
     */
    public WorkerAffichable genererWorker(DTOFormat formatCalcule) {
        // todo ajouter le vrai Worker
        return new WorkerTest("calcul", 500);
    }

    /**
     * appelé par la fenêtre de gestion des formats
     * la fenêtre s'autogère
     */
    public void lancerWorker() {

    }

    /**
     * appelé par la fenêtre de gestion des formats
     * la fenêtre s'autogère
     */
    public void arreterWorker() {
    }

    /**
     * @return la liste des formats de poker disponibles pour la création de format
     */
    public String[] formatsDisponibles() {
        return new String[]{
                Variante.PokerFormat.CASH_GAME.toString(),
                Variante.PokerFormat.MTT.toString(),
                Variante.PokerFormat.SPIN.toString()
        };
    }
}
