package analyzor.vue.couleurs;

import java.awt.*;

/**
 * stocke les couleurs des actions
 * permet d'attribuer automatiquement des couleurs graduées au Raise
 */
public class CouleursActions {
    //todo mettre les bonnes couleurs
    public static Color ACTION_NON_DEFINIE = Color.GRAY;
    public static Color FOLD = Color.BLUE;
    public static Color CALL = Color.GREEN;
    public static Color ALL_IN = Color.BLACK;

    public CouleursActions() {

    }

    /**
     * méthode qui fournit des couleurs de RAISE qui varient au fur et à mesure
     * @return une couleur
     */
    public Color raiseSuivant() {
        // todo faire le générateur de couleur
        return Color.RED;
    }

}
