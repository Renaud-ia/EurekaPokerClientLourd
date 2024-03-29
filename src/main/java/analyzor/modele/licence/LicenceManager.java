package analyzor.modele.licence;

import analyzor.modele.berkeley.EnregistrementLicence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;


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

    
    public int licenceActivee() {
        return licenceActivee;
    }

    public boolean modeDemo() {
        return licenceActivee != 0;
    }

    public String getCleLicence() {
        return cleLicence;
    }

    
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

