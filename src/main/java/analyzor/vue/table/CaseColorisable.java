package analyzor.vue.table;

import analyzor.vue.couleurs.CouleursActions;
import analyzor.vue.donnees.RangeVisible;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

/**
 * classe de base coloriée en fonction des actions
 * va être actualisée dès qu'on fera repaint() sur le composant
 */
public abstract class CaseColorisable extends JPanel implements MouseListener {
    private final LinkedList<RangeVisible.ActionVisible> actionVisibles;
    public CaseColorisable(LinkedList<RangeVisible.ActionVisible> actionVisibles) {
        this.actionVisibles = actionVisibles;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int positionX = 0;

        for (RangeVisible.ActionVisible actionVisible : actionVisibles) {
            Color couleur = actionVisible.getCouleur();
            int largeurX = Math.round(actionVisible.getPourcentage() * this.getWidth());

            g.setColor(couleur);
            g.fillRect(positionX, 0, largeurX, this.getHeight());
            positionX += largeurX;

            if (positionX > getWidth()) {
                throw new RuntimeException("On a dépassé le cadre");
            }
        }

        if (positionX < this.getWidth()) {
            g.setColor(CouleursActions.ACTION_NON_DEFINIE);
            g.fillRect(positionX, 0, this.getWidth(), this.getHeight());
        }
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

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
