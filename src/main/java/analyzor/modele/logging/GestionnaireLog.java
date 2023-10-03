package analyzor.modele.logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

public class GestionnaireLog {

    private enum Mode {
        DEVELOPPEMENT, DEBUG, TEST, PRODUCTION
    }
    private static Mode mode = Mode.DEVELOPPEMENT;

    //emplacements
    private static String currentDirectory = System.getProperty("user.dir");
    private static File dossierLogs = new File(currentDirectory + File.separator + "logs");

    //handlers
    public static FileHandler warningBDD;
    public static FileHandler warningImport;
    public static FileHandler bugSysteme;
    public static FileHandler debugBDD;

    private static ConsoleHandler consoleHandler = new ConsoleHandler();

    static {
        if (!dossierLogs.exists()) {
            dossierLogs.mkdir();
        }
        try {
            // on définit le format des logs
            System.setProperty("java.util.logging.SimpleFormatter.format",
                    "%1$tF %1$tT %4$s %3$s %5$s%n");
            warningBDD = new FileHandler(dossierLogs + File.separator + "warning_bdd.txt", true);
            warningBDD.setLevel(Level.WARNING);

            warningImport = new FileHandler(dossierLogs + File.separator + "warning_import.txt", true);
            warningImport.setLevel(Level.WARNING);

            bugSysteme = new FileHandler(dossierLogs + File.separator + "bug_systeme.txt", true);
            warningImport.setLevel(Level.SEVERE);

            debugBDD = new FileHandler(dossierLogs + File.separator + "debug_bdd.txt", true);
            warningImport.setLevel(Level.ALL);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger(String nomLogger) {
        Logger logger = Logger.getLogger(nomLogger);
        if (mode == Mode.DEVELOPPEMENT) {
            logger.setLevel(Level.ALL);
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFilter(record -> record.getLevel().intValue() < Level.INFO.intValue());
        }
        else if (mode == Mode.DEBUG) {
            logger.setLevel(Level.FINE);
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFilter(record -> record.getLevel().intValue() < Level.INFO.intValue()
                    && record.getLevel().intValue() > Level.FINE.intValue());
        }
        else if (mode == Mode.TEST) {
            logger.setLevel(Level.INFO);
            consoleHandler.setLevel(Level.OFF);
        }
        else if (mode == Mode.PRODUCTION) {
            logger.setLevel(Level.WARNING);
            consoleHandler.setLevel(Level.OFF);
        }
        logger.addHandler(consoleHandler);
        return logger;
    }

    public static void setHandler(Logger logger, Handler handler) {
        SimpleFormatter simpleFormatter = new SimpleFormatter();
        handler.setFormatter(simpleFormatter);
        logger.addHandler(handler);
    }

    public static void main(String[] args) {
        Logger logger = GestionnaireLog.getLogger("Loggertest");
        GestionnaireLog.setHandler(logger, GestionnaireLog.warningBDD);
        logger.info("TEST");
    }
}
