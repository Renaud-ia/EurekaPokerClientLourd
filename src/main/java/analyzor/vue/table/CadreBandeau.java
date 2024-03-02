package analyzor.vue.table;

import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.basiques.Polices;
import analyzor.vue.reutilisables.PanneauFonceArrondi;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * cadre de base pour le bandeau du haut
 * change la souris quand on passe dessus
 */
public abstract class CadreBandeau extends PanneauFonceArrondi implements MouseListener {
    protected boolean selectionne;
    public final static int hauteur = 160;
    protected Color couleurFond;
    public CadreBandeau(String name) {
        super();
        selectionne = false;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel titre = new JLabel(name);
        titre.setFont(Polices.titre);
        titre.setForeground(Polices.BLANC_CLAIR);
        this.add(titre);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        couleurFond = CouleursDeBase.PANNEAU_FONCE;
        setOpaque(false);
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Change le curseur lorsque la souris entre dans le JPanel
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (selectionne) couleurFond = CouleursDeBase.PANNEAU_SELECTIONNE;
        else couleurFond = CouleursDeBase.PANNEAU_SURVOLE;
        this.repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Rétablit le curseur par défaut lorsque la souris quitte le JPanel
        this.setCursor(Cursor.getDefaultCursor());
        if (selectionne) couleurFond = CouleursDeBase.PANNEAU_SELECTIONNE;
        else couleurFond = CouleursDeBase.PANNEAU_FONCE;
        this.repaint();
    }

    // bizarrement si on laisse la classe parente faire la même chose, ça ne marche pas!
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
