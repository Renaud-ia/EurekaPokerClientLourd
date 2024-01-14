package analyzor.vue.table;

import analyzor.vue.couleurs.CouleursActions;
import analyzor.vue.donnees.RangeVisible;

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
    private final LinkedList<RangeVisible.ActionVisible> actionVisibles;
    public CaseColorisable(LinkedList<RangeVisible.ActionVisible> actionVisibles) {
        this.setLayout(null);
        this.actionVisibles = actionVisibles;
        this.setPreferredSize(new Dimension(60, 40));
        this.setMinimumSize(new Dimension(60, 40));

        Border bordure = BorderFactory.createLineBorder(Color.BLACK, 1);
        this.setBorder(bordure);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int positionX = 0;
        int totalWidth = 0; // Initialisez la somme des largeurs à zéro

        for (int i = actionVisibles.size() - 1; i >= 0; i--) {
            RangeVisible.ActionVisible actionVisible = actionVisibles.get(i);
            Color couleur = actionVisible.getCouleur();
            int largeurX = Math.round(actionVisible.getPourcentage() * this.getWidth());

            if (largeurX == 0) continue;

            // on s'assure la somme des largeurs calculées ne dépasse pas la largeur du composant
            if (totalWidth + largeurX > getWidth()) {
                largeurX = getWidth() - totalWidth;
            }

            int debutX = positionX;
            positionX += largeurX;
            totalWidth += largeurX;

            System.out.println("CARRE ACTION " + actionVisible.getNom() + " de coordonnées (" + debutX + " - " + positionX + ")");

            g.setColor(couleur);
            g.fillRect(debutX, 0, positionX, this.getHeight());

        }

        if (positionX < getWidth()) {
            g.setColor(CouleursActions.ACTION_NON_DEFINIE);
            g.fillRect(positionX, 0, getWidth(), this.getHeight());
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
        // Change le curseur lorsque la souris entre dans le JPanel
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Rétablit le curseur par défaut lorsque la souris quitte le JPanel
        this.setCursor(Cursor.getDefaultCursor());
    }
}
