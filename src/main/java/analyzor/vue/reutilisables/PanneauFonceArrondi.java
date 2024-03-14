package analyzor.vue.reutilisables;

import analyzor.vue.basiques.CouleursDeBase;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class PanneauFonceArrondi extends JPanel {
    protected int MARGE_VERTICALE = 8;
    protected int MARGE_HORIZONTALE = 10;
    protected final static int TAILLE_ARRONDI = 10;
    protected Color couleurFond;
    public PanneauFonceArrondi() {
        super();
        couleurFond = CouleursDeBase.PANNEAU_FONCE;
        setOpaque(false);
        repaint();
    }

    protected void creerBordures() {
        Border marge = BorderFactory.createEmptyBorder(
                MARGE_VERTICALE, MARGE_HORIZONTALE, MARGE_VERTICALE, MARGE_HORIZONTALE);
        this.setBorder(marge);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dessine un rectangle arrondi rempli avec la couleur de fond
        g2d.setColor(couleurFond);
        g2d.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, TAILLE_ARRONDI, TAILLE_ARRONDI);

        g2d.dispose();
        creerBordures();
    }
}
