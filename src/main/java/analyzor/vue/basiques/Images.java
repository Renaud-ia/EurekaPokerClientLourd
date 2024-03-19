package analyzor.vue.basiques;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.net.URL;

public class Images {
    private static final int LARGEUR_ICONE_BOUTON = 16;
    private static final int HAUTEUR_ICONE_BOUTON = 16;
    // général
    public static Image icone;
    public static Image logo;

    // import
    public static Image ajouterDossier;
    public static Image supprimerDossier;
    public static Image detection;
    public static Image rafraichir;
    public static Image lancerImport;
    public static Image logoBetclic;
    public static Image logoWinamax;

    // formats
    public static Image consulterResultats;
    public static Image ajouterFormat;
    public static Image supprimerFormat;
    public static Image gererFormat;
    public static Image calculerFormat;
    public static Image reinitialiserFormat;

    // autre
    public static Image toopTipInterrogation;


    static  {
        icone = getImage("/images/icone.png");
        logo = getImage("/images/logo_page_principale.png");

        ajouterDossier = getIconeBouton("/images/ajouter_dossier.png");
        supprimerDossier = getIconeBouton("/images/supprimer_dossier.png");
        detection = getIconeBouton("/images/detection.png");
        rafraichir = getIconeBouton("/images/rafraichir.png");
        lancerImport = getIconeBouton("/images/lancer_import.png");
        logoBetclic = getIconeBouton("/images/betclic_miniature.png");
        logoWinamax = getIconeBouton("/images/winamax_miniature.png");

        consulterResultats = getIconeBouton("/images/consulter_resultats.png");
        ajouterFormat = getIconeBouton("/images/ajouter_format.png");
        supprimerFormat = getIconeBouton("/images/supprimer_format.png");
        gererFormat = getIconeBouton("/images/gerer_format.png");
        calculerFormat = getIconeBouton("/images/calculer_format.png");
        reinitialiserFormat = getIconeBouton("/images/reinitialiser_format.png");

        toopTipInterrogation = getIconeBouton("/images/tooltip_interrogation.png");
    }


    private static Image getImage(String cheminRelatifImage) {
        try {
            URL fichier = Objects.requireNonNull(Images.class.getResource(cheminRelatifImage));
            return new ImageIcon(fichier).getImage();
        }
        catch (Exception e) {
            return null;
        }
    }

    private static Image getIconeBouton(String cheminRelatifImage) {
        return getImageRecadree(cheminRelatifImage, LARGEUR_ICONE_BOUTON, HAUTEUR_ICONE_BOUTON);
    }

    private static Image getImageRecadree(String cheminRelatifImage, int largeur, int hauteur) {
        try {
            URL fichier = Objects.requireNonNull(Images.class.getResource(cheminRelatifImage));
            return new ImageIcon(fichier).getImage().getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(Images.icone);
    }
}
