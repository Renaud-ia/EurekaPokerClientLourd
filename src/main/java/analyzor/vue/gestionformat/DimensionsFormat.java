package analyzor.vue.gestionformat;

import java.awt.*;


public class DimensionsFormat {
    public static int hauteurLigne = 20;
    public static Dimension dNomFormat = new Dimension(80, hauteurLigne);
    public static Dimension dAnte = new Dimension(80, hauteurLigne);
    public static Dimension dBounty = new Dimension(80, hauteurLigne);
    public static Dimension dBuyIn = new Dimension(80, hauteurLigne);
    public static Dimension dJoueurs = new Dimension(80, hauteurLigne);
    public static Dimension dParties = new Dimension(80, hauteurLigne);
    public static Dimension dBoutonAjouter = new Dimension(150, hauteurLigne);
    public static Dimension dBoutonInfo = new Dimension(80, hauteurLigne);
    public static Dimension taillePanneauInfos;

    static {
        int largeurPanneauInfos = 50;
        largeurPanneauInfos += dNomFormat.width;
        largeurPanneauInfos += dAnte.width;
        largeurPanneauInfos += dBounty.width;
        largeurPanneauInfos += dBuyIn.width * 2;
        largeurPanneauInfos += dJoueurs.width;
        largeurPanneauInfos += dParties.width;
        largeurPanneauInfos += dBoutonInfo.width;

        taillePanneauInfos = new Dimension(largeurPanneauInfos, hauteurLigne * 15);
    }
}
