package analyzor.modele.exceptions;

import analyzor.modele.logging.GestionnaireLog;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ErreurCritique extends RuntimeException {

    public ErreurCritique(String message) {
        super(message);
        logErreurCritique(message);
    }

    private void logErreurCritique(String message) {
        Logger logger = GestionnaireLog.getLogger(ErreurCritique.class.getName());
        GestionnaireLog.setHandler(logger, GestionnaireLog.bugSysteme);
        logger.log(Level.SEVERE, message, this);
    }
}
