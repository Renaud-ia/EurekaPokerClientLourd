package analyzor.vue.basiques;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * classe utilitaire de base pour gérer les polices
 * permet de charger les polices du fichier resources
 * et génère une police standard en cas de problème
 */
public class Polices {
    public static Font titre;
    public static Font standard;
    public static Font selectionne;
    public static Font gras;
    public static Font italiquePetit;
    public static Color BLANC_CLAIR = new Color(252, 255, 251);

    public static Color BLANC_TERNE = new Color(220, 239, 236);
    public static Color BLANC_CASSE = new Color(158, 176, 176);


    static {
        try {
            float tailleFont = 16;
            String cheminPolice = "/polices/roboto/Roboto-Bold.ttf";
            titre = chargerPolice(cheminPolice, tailleFont);
        } catch (Exception e) {
            titre = new Font("SansSerif", Font.PLAIN, 12);
        }

        try {
            float tailleFont = 14;
            String cheminPolice = "/polices/roboto/Roboto-Regular.ttf";
            standard = chargerPolice(cheminPolice, tailleFont);
        } catch (Exception e) {
            standard = new Font("SansSerif", Font.PLAIN, 12);
        }

        try {
            float tailleFont = 14;
            String cheminPolice = "/polices/roboto/Roboto-BlackItalic.ttf";
            selectionne = chargerPolice(cheminPolice, tailleFont);
        } catch (Exception e) {
            selectionne = new Font("SansSerif", Font.PLAIN, 12);
        }

        try {
            float tailleFont = 14;
            String cheminPolice = "/polices/roboto/Roboto-Bold.ttf";
            gras = chargerPolice(cheminPolice, tailleFont);
        } catch (Exception e) {
            gras = new Font("SansSerif", Font.PLAIN, 12);
        }

        try {
            float tailleFont = 12;
            String cheminPolice = "/polices/roboto/Roboto-Italic.ttf";
            italiquePetit = chargerPolice(cheminPolice, tailleFont);
        } catch (Exception e) {
            italiquePetit = new Font("SansSerif", Font.PLAIN, 12);
        }
    }

    private static Font chargerPolice(String chemin, float taille) throws IOException, FontFormatException {
        InputStream fichierFont = Objects.requireNonNull(Polices.class.getResourceAsStream(chemin));
        return Font.createFont(Font.TRUETYPE_FONT, fichierFont).deriveFont(taille);
    }


    public static void main(String[] args) {
        System.out.println(Polices.titre);
    }

}
