package analyzor.modele.exceptions;

import analyzor.modele.logging.GestionnaireLog;

import java.util.logging.Logger;

public class ErreurCritique extends Exception {

    public ErreurCritique(String message) {
        super(message);
        logErreurCritique(message);
    }

    private void logErreurCritique(String message) {
        Logger logger = GestionnaireLog.getLogger(ErreurCritique.class.getName());
        GestionnaireLog.setHandler(logger, GestionnaireLog.bugSysteme);
        logger.severe(message);
    }
}
