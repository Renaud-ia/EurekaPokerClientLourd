package analyzor.vue.table;

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
public abstract class CadreBandeau extends JPanel implements MouseListener {
    private final static Color couleurFond = new Color(100, 100, 100);
    protected final static int hauteur = 130;
    protected CompoundBorder bordureBlanche;
    protected CompoundBorder bordureInvisible;
    protected CompoundBorder bordureSurlignee;
    public CadreBandeau(String name) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        creerBordures(name);
        setBackground(couleurFond);
    }

    private void creerBordures(String name) {
        EmptyBorder bordureInterne = new EmptyBorder(5, 5, 5, 5);

        Border bordureBaseBlanche = BorderFactory.createLineBorder(Color.WHITE);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(bordureBaseBlanche, name);
        bordureBlanche = new CompoundBorder(titledBorder, bordureInterne);

        Border bordureBaseInvisible = BorderFactory.createLineBorder(couleurFond);
        TitledBorder titledBorder2 = BorderFactory.createTitledBorder(bordureBaseInvisible, name);
        bordureInvisible = new CompoundBorder(titledBorder2, bordureInterne);

        Border bordureBaseSurlignee = BorderFactory.createLineBorder(Color.BLUE);
        TitledBorder titledBorder3 = BorderFactory.createTitledBorder(bordureBaseSurlignee, name);
        bordureSurlignee = new CompoundBorder(titledBorder3, bordureInterne);
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
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Rétablit le curseur par défaut lorsque la souris quitte le JPanel
        this.setCursor(Cursor.getDefaultCursor());
    }
}
