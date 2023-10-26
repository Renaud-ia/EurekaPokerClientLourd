package analyzor.modele.config;

import java.io.File;

public class ValeursConfig {
    public enum Mode {
        DEVELOPPEMENT, DEBUG, TEST, PRODUCTION, PRODUCTION_DEBUG
    }
    public static Mode mode = Mode.DEVELOPPEMENT;
    // subsets activés ou non
    public static boolean SUBSETS = true;
    // deuxième rank FLOP, on utilise SUBSET ou DYNAMIQUE ?
    public static boolean SUBSETS_2E_RANK = true;
    public static int N_SUBSETS = 15;

    static {
        // on désactive automatiquement subsets 2e rank si on désactive subsets
        SUBSETS_2E_RANK = (SUBSETS && SUBSETS_2E_RANK);
    }
    private static String currentDirectory = System.getProperty("user.dir");

    // stockage des valeurs ressources
    public static File dossierRessourcesProduction = new File(currentDirectory + File.separator + "ressources");
    public static File dossierRessourcesDeveloppement = new File(currentDirectory + File.separator
            + "src" + File.separator + "main" + File.separator + "resources");


}
