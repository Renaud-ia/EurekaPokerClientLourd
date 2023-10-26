package analyzor.modele.logging;

import analyzor.modele.config.ValeursConfig;

import java.io.File;
import java.util.logging.*;

public class GestionnaireLog {
    private static ValeursConfig.Mode mode = ValeursConfig.Mode.DEVELOPPEMENT;

    //emplacements
    private static String currentDirectory = System.getProperty("user.dir");
    private static File dossierLogs = new File(currentDirectory + File.separator + "logs");

    //handlers
    public static FileHandler warningBDD;
    public static FileHandler bugSysteme;
    public static FileHandler debugBDD;
    public static FileHandler importMains;
    public static FileHandler importWinamax;
    public static FileHandler importIpoker;

    private static ConsoleHandler consoleHandler = new ConsoleHandler();

    static {
        if (!dossierLogs.exists()) {
            dossierLogs.mkdir();
        }
        try {
            // on définit le format des logs et le nom des fichiers
            System.setProperty("java.util.logging.SimpleFormatter.format",
                    "%1$tF %1$tT %4$s %3$s %5$s%n");
            //todo : on ne veut pas distinguer les fichiers par niveau mais juste par séparation des tâches
            warningBDD = new FileHandler(dossierLogs + File.separator + "warning_bdd.txt", true);
            bugSysteme = new FileHandler(dossierLogs + File.separator + "bug_systeme.txt", true);
            debugBDD = new FileHandler(dossierLogs + File.separator + "debug_bdd.txt", true);
            importMains = new FileHandler(dossierLogs + File.separator + "import.txt", true);
            importWinamax = new FileHandler(dossierLogs + File.separator + "import_winamax.txt", true);
            importIpoker = new FileHandler(dossierLogs + File.separator + "import_ipoker.txt", true);

        } catch (Exception e) {
            //todo : qu'est ce qu'on fait????
            throw new RuntimeException("Impossible d'initialiser les logs", e);
        }

        if (mode == ValeursConfig.Mode.DEVELOPPEMENT) {
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFilter(record -> record.getLevel().intValue() <= Level.INFO.intValue());
        }
        else if (mode == ValeursConfig.Mode.DEBUG) {
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFilter(record -> record.getLevel().intValue() <= Level.INFO.intValue()
                    && record.getLevel().intValue() > Level.FINE.intValue());
        }
        else if (mode == ValeursConfig.Mode.TEST) {
            consoleHandler.setLevel(Level.INFO);
        }
        else if (mode == ValeursConfig.Mode.PRODUCTION) {
            consoleHandler.setLevel(Level.OFF);
        }
        else if (mode == ValeursConfig.Mode.PRODUCTION_DEBUG) {
            consoleHandler.setLevel(Level.OFF);
        }
    }

    public static Logger getLogger(String nomLogger) {
        Logger logger = Logger.getLogger(nomLogger);
        Handler[] handlers = logger.getHandlers();
        if (handlers.length > 0) {
            return logger;
        }
        if (mode == ValeursConfig.Mode.DEVELOPPEMENT) {
            logger.setLevel(Level.ALL);
        }
        else if (mode == ValeursConfig.Mode.DEBUG) {
            logger.setLevel(Level.FINE);
        }
        else if (mode == ValeursConfig.Mode.TEST) {
            logger.setLevel(Level.INFO);
        }
        else if (mode == ValeursConfig.Mode.PRODUCTION) {
            logger.setLevel(Level.WARNING);
        }
        else if (mode == ValeursConfig.Mode.PRODUCTION_DEBUG) {
            logger.setLevel(Level.FINE);
        }
        logger.addHandler(consoleHandler);
        return logger;
    }

    public static void setHandler(Logger logger, Handler handler) {
        Handler[] handlers = logger.getHandlers();
        for (Handler handlerExistant: handlers) {
            if (handlerExistant.getClass().equals(handler.getClass())) return;
        }
        SimpleFormatter simpleFormatter = new SimpleFormatter();
        handler.setFormatter(simpleFormatter);
        logger.addHandler(handler);
    }
}
