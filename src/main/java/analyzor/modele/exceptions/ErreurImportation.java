package analyzor.modele.exceptions;

import analyzor.modele.logging.GestionnaireLog;

import java.util.logging.Logger;

public class ErreurImportation extends Exception {
    // on capture l'erreur au niveau du modèle et on indique le nombre d'erreurs au controleur
    // on log pour que l'user puisse nous fournir le fichier
    private static final Logger logger = GestionnaireLog.getLogger("ErreurImportation");
    static {
        GestionnaireLog.setHandler(logger, GestionnaireLog.importMains);
    }

    public ErreurImportation(String message) {
        super(message);
        logger.warning(message);
    }
}
