package analyzor.controleur;

import analyzor.modele.licence.LicenceManager;
import analyzor.vue.donnees.licence.LicenceDTO;
import analyzor.vue.licence.FenetreLicence;

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

    private void rafraichirLicence() {
        // todo notifier le problème de connexion lors de l'écran d'accueil
        String cleLicence = LicenceManager.getInstance().getCleLicence();
        int licenceActivee = LicenceManager.getInstance().licenceActivee();
        licenceDTO.setCleLicence(cleLicence);
        licenceDTO.setStatutLicence(licenceActivee);
        fenetreLicence.rafraichir();

        problemeLicence = licenceActivee > 0;
    }

    public void activerLicence(String cleLicence) {
        int codeActivation = LicenceManager.getInstance().activerLicence(cleLicence);

        if (codeActivation == 0) {
            rafraichirLicence();
            fenetreLicence.messageInfo("Licence ajoutée avec succès");
        }

        else if (codeActivation == 1) {
            fenetreLicence.messageErreur("Impossible de se connecter au serveur");
        }

        else if (codeActivation == 2) {
            fenetreLicence.messageErreur("La licence n'est pas valide");
        }

        else if (codeActivation == 3) {
            fenetreLicence.messageErreur("La licence a déjà été activée");
        }

        else {
            fenetreLicence.messageErreur("Une erreur inconnue est survenue lors de l'activation");
        }
    }

    public void supprimerLicence() {
        LicenceManager.getInstance().supprimerLicence();
        rafraichirLicence();

        fenetreLicence.messageInfo("Licence supprimée avec succès");
    }

    @Override
    public void lancerVue() {
        fenetreLicence.setVisible(true);
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
