package analyzor.controleur;

import analyzor.modele.estimation.Estimateur;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.GestionnaireFormat;
import analyzor.modele.parties.Variante;
import analyzor.vue.donnees.format.DTOFormat;
import analyzor.vue.gestionformat.FenetreFormat;
import analyzor.vue.FenetrePrincipale;
import analyzor.vue.reutilisables.fenetres.FenetreChargement;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;

/**
 * interface de création des formats et de calcul
 */
public class ControleurFormat implements ControleurSecondaire {
    private final ControleurPrincipal controleurPrincipal;
    private final FenetreFormat vue;
    private Estimateur workerCalcul;

    private final HashMap<FormatSolution, DTOFormat> formatModeleVersVue;
    private final HashMap<DTOFormat, FormatSolution> formatVueVersModele;
    private FormatSolution formatEnCoursDeCalcul;

    public ControleurFormat(FenetrePrincipale fenetrePrincipale, ControleurPrincipal controleur) {
        this.controleurPrincipal = controleur;
        this.vue = new FenetreFormat(fenetrePrincipale, this);

        formatModeleVersVue = new HashMap<>();
        formatVueVersModele = new HashMap<>();
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
                    format.getNombreSituations(),
                    format.getNombreSituationsResolues(),
                    format.getNombreParties(),
                    format.getNombresPartiesCalculees(),
                    format.getPreflopCalcule(),
                    format.getFlopCalcule(),
                    format.getPctAvancement()
            );

            vue.ajouterFormat(formatTrouve);

            formatModeleVersVue.put(format, formatTrouve);
            formatVueVersModele.put(formatTrouve, format);

        }
        this.vue.actualiser();
    }

    @Override
    public void lancerVue() {
        this.vue.afficher();
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

            formatModeleVersVue.put(formatCree, infosFormat);
            formatVueVersModele.put(infosFormat, formatCree);

            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void formatSelectionne(DTOFormat infosFormat) {
        desactiverVue();
        FormatSolution formatSolution = GestionnaireFormat.getFormatSolution(infosFormat.getIdBDD());
        controleurPrincipal.formatSelectionne(formatSolution);
    }

    public void reinitialiser(DTOFormat formatModifie) {
        final FenetreChargement fenetreChargement =
                new FenetreChargement(vue.getFenetreGestion(), "R\u00E9initialisation en cours...");

        Thread reinitialisation = new Thread(() -> {
            SwingUtilities.invokeLater(fenetreChargement::lancer);
            FormatSolution formatSolution = formatVueVersModele.get(formatModifie);
            if (formatSolution == null) throw new RuntimeException("Format non trouvé");
            GestionnaireFormat.reinitialiserFormat(formatSolution);
            formatModifie.setNonCalcule();
            SwingUtilities.invokeLater(fenetreChargement::arreter);
            this.vue.actualiser();
        });

        reinitialisation.start();

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
    public JProgressBar genererWorker(DTOFormat formatCalcule) {
        // garder en mémoire le format calculé
        formatEnCoursDeCalcul = formatVueVersModele.get(formatCalcule);
        if (formatEnCoursDeCalcul == null) throw new IllegalArgumentException("Format non trouvé");

        workerCalcul = new Estimateur(formatEnCoursDeCalcul);
        return workerCalcul.getProgressBar();
    }

    /**
     * appelé par la fenêtre de gestion des formats
     * la fenêtre s'autogère
     */
    public void lancerWorker(DTOFormat format) {
        workerCalcul.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName())) {
                if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                    calculTermine(format, workerCalcul.isCancelled());
                    vue.calculActive(true);
                }
            }
        });

        vue.calculActive(false);
        workerCalcul.execute();
    }

    /**
     * procédure d'actualisation du format
     */
    private void calculTermine(DTOFormat format, boolean annule) {
        rechargerInfosFormat(format);

        // on prévient Fenetre Format que le calcul est terminé
        vue.calculTermine(annule);
    }

    private void rechargerInfosFormat(DTOFormat format) {
        FormatSolution formatSolution = GestionnaireFormat.getFormatSolution(format.getIdBDD());

        format.setNombreParties(formatSolution.getNombreParties());
        format.setNombrePartiesCalculees(formatSolution.getNombresPartiesCalculees());

        format.setNombreSituations(formatSolution.getNombreSituations());
        format.setNombreSituationsCalculees(formatSolution.getNombreSituationsResolues());

        format.setPreflopCalcule(formatSolution.getPreflopCalcule());
        format.setFlopCalcule(formatSolution.getFlopCalcule());
        format.setPctAvancement(formatSolution.getPctAvancement());
    }

    /**
     * appelé par la fenêtre de gestion des formats
     * la fenêtre s'autogère
     */
    public void arreterWorker() {
        workerCalcul.annulerTache();
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
