package analyzor.modele.licence;

import analyzor.modele.berkeley.EnregistrementLicence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * classe qui assure l'interface de la licence avec le controleur
 * et gère l'inscription récupération des données licences dans BDD
 * implémente le pattern singleton => non safe pour multithreading
 */
public class LicenceManager {
    private final static Logger logger = LogManager.getLogger(LicenceManager.class);
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
     * @return 0 si licence activée, 1 si vérification impossible, 2 sinon
     */
    public int licenceActivee() {
        return licenceActivee;
    }

    public boolean modeDemo() {
        return licenceActivee != 0;
    }

    public String getCleLicence() {
        recupererInfosLicence();
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

    private void recupererInfosLicence() {
        cleLicence = enregistrementLicence.getCleLicence();
        String cleMachine = enregistrementLicence.getCleMachine();

        if (cleLicence == null || cleMachine == null) licenceActivee = 1;

        // todo est ce qu'on veut pas vérifier la clé licence de manière random
        else {
            if (connexionServeur.connexionImpossible()) {
                licenceActivee = 1;
            }

            else if((CleMachine.verifier(cleMachine) && verifierLicence(cleLicence))) {
                licenceActivee = 0;
            }

            else {
                // si les clés ne correspondent pas on les supprime
                enregistrementLicence.supprimerCles();
                licenceActivee = 2;
                logger.trace("Les clés récupérés sont incorrectes, on les supprime");
            }
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
            logger.warn("La connexion au serveur n'est pas active");
            return true;
        }

        return connexionServeur.verifierLicence(cleLicence);
    }
}

