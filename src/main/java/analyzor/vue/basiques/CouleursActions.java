package analyzor.vue.basiques;

import java.awt.*;

/**
 * stocke les couleurs des actions
 * permet d'attribuer automatiquement des couleurs graduées au Raise
 */
public class CouleursActions {
    //todo mettre les bonnes couleurs
    public static Color ACTION_NON_DEFINIE = new Color(56, 56, 56);
    public static Color CASE_SURVOLEE = new Color(86, 86, 86);
    public static Color FOLD = new Color(89, 162, 213);
    public static Color CALL = new Color(78, 164, 115);
    public static Color ALL_IN = new Color(36,9, 2);

    public int compteRaise;

    public static Color[] RAISES = new Color[] {
            new Color(206, 50, 50),
            new Color(125,14,19),
            new Color(98,13,20),
            new Color(55,4,14),

    };

    public CouleursActions() {
        compteRaise = 0;
    }

    /**
     * méthode qui fournit des couleurs de RAISE qui varient au fur et à mesure
     * @return une couleur
     */
    public Color raiseSuivant() {
        return RAISES[compteRaise++];
    }

}
