package analyzor.controleur;

import analyzor.modele.licence.LicenceManager;
import analyzor.vue.donnees.licence.LicenceDTO;
import analyzor.vue.licence.FenetreLicence;
import analyzor.vue.reutilisables.fenetres.FenetreChargement;

import javax.swing.*;

public class ControleurLicence implements ControleurSecondaire {
    private final FenetreLicence fenetreLicence;
    private final LicenceDTO licenceDTO;
    private boolean problemeLicence;

    public ControleurLicence(JFrame fenetrePrincipale) {
        this.licenceDTO = new LicenceDTO();
        this.fenetreLicence = new FenetreLicence(fenetrePrincipale, licenceDTO, this);
        problemeLicence = false;
    }

    @Override
    public void demarrer() {
        rafraichirLicence();
    }

    private int rafraichirLicence() {
        String cleLicence = LicenceManager.getInstance().getCleLicence();
        int licenceActivee = LicenceManager.getInstance().licenceActivee();
        licenceDTO.setCleLicence(cleLicence);
        licenceDTO.setStatutLicence(licenceActivee);
        fenetreLicence.rafraichir();

        problemeLicence = licenceActivee > 0;

        return licenceActivee;
    }

    public void reverifierLicence() {
        final FenetreChargement fenetreChargement =
                new FenetreChargement(fenetreLicence, "Vérification de la licence...");

        Thread verificationLicence = new Thread(() -> {
            SwingUtilities.invokeLater(fenetreChargement::lancer);
            int codeActivation = rafraichirLicence();


            if (codeActivation == 0) {
                rafraichirLicence();
                fenetreLicence.messageInfo("Licence vérifiée avec succès");
            } else if (codeActivation == 1) {
                fenetreLicence.messageErreur("Impossible de se connecter au serveur");
            } else if (codeActivation == 2) {
                fenetreLicence.messageErreur("La licence n'est pas valide");
            } else if (codeActivation == 3) {
                fenetreLicence.messageErreur("La licence a déjà été activée");
            } else {
                fenetreLicence.messageErreur("Une erreur inconnue est survenue lors de l'activation");
            }
            SwingUtilities.invokeLater(fenetreChargement::arreter);
        });
        verificationLicence.start();
    }

    public void activerLicence(String cleLicence) {
        final FenetreChargement fenetreChargement =
                new FenetreChargement(fenetreLicence, "Activation de la licence...");

        Thread activationLicence = new Thread(() -> {
            SwingUtilities.invokeLater(fenetreChargement::lancer);
            int codeActivation = LicenceManager.getInstance().activerLicence(cleLicence);

            if (codeActivation == 0) {
                rafraichirLicence();
                fenetreLicence.messageInfo("Licence ajoutée avec succès");
            } else if (codeActivation == 1) {
                fenetreLicence.messageErreur("Impossible de se connecter au serveur");
            } else if (codeActivation == 2) {
                fenetreLicence.messageErreur("La licence n'est pas valide");
            } else if (codeActivation == 3) {
                fenetreLicence.messageErreur("La licence a déjà été activée");
            } else {
                fenetreLicence.messageErreur("Une erreur inconnue est survenue lors de l'activation");
            }
            SwingUtilities.invokeLater(fenetreChargement::arreter);
        });
        activationLicence.start();
    }

    public void supprimerLicence() {
        LicenceManager.getInstance().supprimerLicence();
        rafraichirLicence();
        fenetreLicence.licenceSupprimee();

        fenetreLicence.messageInfo("Licence supprimée avec succès");
    }

    @Override
    public void lancerVue() {
        fenetreLicence.afficher();
    }

    @Override
    public void desactiverVue() {
        fenetreLicence.setVisible(false);
    }

    /**
     * utilisé par controleur principal pour savoir si il y a un problème avec la licence
     * @return true si problème, false sinon
     */
    public boolean problemeLicence() {
        return problemeLicence;
    }
}
