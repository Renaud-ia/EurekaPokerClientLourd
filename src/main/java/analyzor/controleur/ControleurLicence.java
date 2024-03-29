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
                new FenetreChargement(fenetreLicence, "V\u00E9rification de la licence...");

        Thread verificationLicence = new Thread(() -> {
            SwingUtilities.invokeLater(fenetreChargement::lancer);
            int codeActivation = rafraichirLicence();


            if (codeActivation == 0) {
                rafraichirLicence();
                fenetreLicence.messageInfo("Licence v\u00E9rifi\u00E9e avec succ\u00E8s");
            } else if (codeActivation == 1) {
                fenetreLicence.messageErreur("Impossible de se connecter au serveur");
            } else if (codeActivation == 2) {
                fenetreLicence.messageErreur("La licence n'est pas valide");
            } else if (codeActivation == 3) {
                fenetreLicence.messageErreur("La licence a déjà été activ\u00E9e");
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
                fenetreLicence.messageInfo("Licence ajout\u00E9e avec succ\u00E8s");
            } else if (codeActivation == 1) {
                fenetreLicence.messageErreur("Impossible de se connecter au serveur");
            } else if (codeActivation == 2) {
                fenetreLicence.messageErreur("La licence n'est pas valide");
            } else if (codeActivation == 3) {
                fenetreLicence.messageErreur("La licence a d\u00E9j\u00E0 \u00E9t\u00E9 activ\u00E9e");
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

        fenetreLicence.messageInfo("Licence supprim\u00E9e avec succ\u00E8s");
    }

    @Override
    public void lancerVue() {
        fenetreLicence.afficher();
    }

    @Override
    public void desactiverVue() {
        fenetreLicence.setVisible(false);
    }

    
    public boolean problemeLicence() {
        return problemeLicence;
    }
}
