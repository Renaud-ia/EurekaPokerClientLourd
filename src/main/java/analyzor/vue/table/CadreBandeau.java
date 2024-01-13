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
public class CadreBandeau extends JPanel implements MouseListener {
    public CadreBandeau(String name) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        EmptyBorder bordureInterne = new EmptyBorder(5, 5, 5, 5);
        Border bordureArrondie = BorderFactory.createLineBorder(Color.GRAY);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(bordureArrondie, name);
        CompoundBorder bordureTotale = new CompoundBorder(titledBorder, bordureInterne);
        setBorder(bordureTotale);
        setBackground(Color.WHITE);
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
