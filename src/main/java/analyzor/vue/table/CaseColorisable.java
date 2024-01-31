package analyzor.vue.table;

import analyzor.vue.donnees.table.RangeVisible;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

/**
 * classe de base coloriée en fonction des actions
 * va être actualisée dès qu'on fera repaint() sur le composant
 */
public abstract class CaseColorisable extends JPanel implements MouseListener {
    protected final LinkedList<RangeVisible.ActionVisible> actionVisibles;
    public CaseColorisable(LinkedList<RangeVisible.ActionVisible> actionVisibles) {
        this.setLayout(null);
        this.actionVisibles = actionVisibles;

        Border bordure = BorderFactory.createLineBorder(new Color(47, 47, 47), 1);
        this.setBorder(bordure);

        this.addMouseListener(this);
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
