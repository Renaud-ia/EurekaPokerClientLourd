package analyzor.modele.licence;

import analyzor.modele.berkeley.EnregistrementLicence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/**
 * classe qui assure l'interface de la licence avec le controleur
 * et gère l'inscription récupération des données licences dans BDD
 * implémente le pattern singleton => non safe pour multithreading
 * Valeurs de licence activée :
 * -1 si pas de clé, 0 si clé bonne, 1 si connexion impossible, 2 si mauvaise clé, 3 si clé déjà activée
 */
public class LicenceManager {
    private final static float FREQUENCE_VERIFICATION_LICENCE = 0.1f;
    private static LicenceManager instanceManager;
    private final ConnexionServeur connexionServeur;
    private final EnregistrementLicence enregistrementLicence;
    private int licenceActivee;
    private String cleLicence;
    private LicenceManager() {
        this.connexionServeur = new ConnexionServeur();
        this.enregistrementLicence = new EnregistrementLicence();
        recupererInfosLicence();
    }

    public static LicenceManager getInstance() {
        if (instanceManager == null) {
            instanceManager = new LicenceManager();
        }

        return instanceManager;
    }

    /**
     * renvoie le statut de la licence
     * @return -1 si pas de licence, 0 si licence activée, 1 si vérification impossible, 2 sinon
     */
    public int licenceActivee() {
        return licenceActivee;
    }

    public boolean modeDemo() {
        return licenceActivee != 0;
    }

    public String getCleLicence() {
        return cleLicence;
    }

    /**
     * essaye d'activer la licence en ligne
     * @param cleLicence clé à activer
     * @return 0 si c'est bon, 1 si connexion inactive, 2 si mauvaise clé, 3 si clé déjà activée
     */
    public int activerLicence(String cleLicence) {
        if (connexionServeur.connexionImpossible()) {
            return 1;
        }

        int codeActivation = connexionServeur.activerLicence(cleLicence);
        this.licenceActivee = codeActivation;

        if (codeActivation == 0) {
            enregistrementLicence.enregistrerCleLicence(cleLicence);
            enregistrementLicence.enregistrerCleMachine(CleMachine.generer());
            this.cleLicence = cleLicence;
        }

        return codeActivation;
    }

    public void supprimerLicence() {
        enregistrementLicence.supprimerCleLicence();
    }

    /**
     * -1 si pas de licence, 0 si licence activée, 1 si pas de connexion, 2 si mauvaise clé
     */
    private void recupererInfosLicence() {
        cleLicence = enregistrementLicence.getCleLicence();
        String cleMachine = enregistrementLicence.getCleMachine();

        if (cleLicence == null || cleMachine == null) {
            licenceActivee = -1;
            return;
        }

        Random random = new Random();
        float randomValeur = random.nextFloat();

        if (randomValeur > FREQUENCE_VERIFICATION_LICENCE) {
            licenceActivee = 0;
            return;
        }

        if (connexionServeur.connexionImpossible()) {
            licenceActivee = 1;
        }

        // clé machine ne correspond pas, on reset la licence
        else if (!CleMachine.verifier(cleMachine)) {
            enregistrementLicence.supprimerCles();
            licenceActivee = -1;
        }

        else if(!verifierLicence(cleLicence)) {
            licenceActivee = 2;
        }

        else {
            licenceActivee = 0;
        }

    }

    /**
     * void
     * vérification de la licence sur le serveur
     * return true si la connexion serveur n'est pas active
     * @param cleLicence clé à vérifier
     * @return vérification ok
     */
    private boolean verifierLicence(String cleLicence) {
        if (connexionServeur.connexionImpossible()) {
            return true;
        }

        return connexionServeur.verifierLicence(cleLicence);
    }

    public boolean connexionRatee() {
        return licenceActivee == 1;
    }
}

