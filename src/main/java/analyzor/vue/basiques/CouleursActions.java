package analyzor.vue.basiques;

import java.awt.*;


public class CouleursActions {
    public static Color ACTION_NON_DEFINIE = new Color(56, 56, 56);
    public static Color CASE_SURVOLEE = new Color(86, 86, 86);
    public static Color FOLD = new Color(89, 162, 213);
    public static Color CALL = new Color(76, 176, 111);
    public static Color ALL_IN = new Color(26, 2, 2);

    public int compteRaise;

    public static Color[] RAISES = new Color[] {
            new Color(206, 50, 50),
            new Color(164, 34, 34),
            new Color(119, 17, 17),
            new Color(77, 11, 11),

    };

    public CouleursActions() {
        compteRaise = 0;
    }

    
    public Color raiseSuivant() {
        return RAISES[compteRaise++];
    }

}
