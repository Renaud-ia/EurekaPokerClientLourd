package analyzor.vue.table;

import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.basiques.Polices;
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
    protected boolean survole;
    protected LinkedList<RangeVisible.ActionVisible> actionVisibles;
    public CaseColorisable() {
        this.setLayout(null);

        Border bordure = BorderFactory.createLineBorder(CouleursDeBase.BORDURE_FONCEE, 1);
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
        survole = true;
        this.repaint();

        for (Component composant : getComponents()) {
            if (composant instanceof JLabel) {
                composant.setFont(Polices.selectionne);
                composant.setForeground(Polices.BLANC_CLAIR);
            }
        }
        Border bordure = BorderFactory.createLineBorder(Polices.BLANC_CLAIR, 2);
        this.setBorder(bordure);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Rétablit le curseur par défaut lorsque la souris quitte le JPanel
        this.setCursor(Cursor.getDefaultCursor());
        survole = false;
        this.repaint();

        for (Component composant : getComponents()) {
            if (composant instanceof JLabel) {
                composant.setFont(Polices.standard);
                composant.setForeground(Polices.BLANC_TERNE);
            }
        }

        Border bordure = BorderFactory.createLineBorder(CouleursDeBase.BORDURE_FONCEE, 1);
        this.setBorder(bordure);
    }
}
