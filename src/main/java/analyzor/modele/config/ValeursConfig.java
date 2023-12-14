package analyzor.modele.config;

import java.io.File;

public class ValeursConfig {
    public static String nomProfilHero = "hero";
    public static final int MAX_JOUEURS = 10;

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



    // poids relatif du effectif SRP et du pot bounty dans le clustering SPRB
    public static float[] poidsSPRB = {1f, 1f, 1f};
    // taille minimum des clusters SPRB
    public static int effectifMinClusterSRPB = 300;


    // stockage des valeurs ressources
    public static File dossierRessourcesProduction = new File(currentDirectory + File.separator + "ressources");
    public static File dossierRessourcesDeveloppement = new File(currentDirectory + File.separator
            + "src" + File.separator + "main" + File.separator + "resources");


}
