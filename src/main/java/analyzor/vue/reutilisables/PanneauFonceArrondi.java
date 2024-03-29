package analyzor.vue.reutilisables;

import analyzor.vue.basiques.CouleursDeBase;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanneauFonceArrondi extends JPanel {
    protected int BORDURE = 10;
    protected final static int TAILLE_ARRONDI = 10;
    protected Color couleurFond;
    public PanneauFonceArrondi() {
        super();
        couleurFond = CouleursDeBase.PANNEAU_FONCE;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            g2d.setColor(couleurFond);
            g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, TAILLE_ARRONDI, TAILLE_ARRONDI);

        } finally {
            g2d.dispose();
        }
    }
}
