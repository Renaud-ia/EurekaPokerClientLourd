package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.couleurs.CouleursActions;
import analyzor.vue.donnees.table.RangeVisible;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class CaseCombo extends CaseColorisable {
    private final ControleurTable controleur;
    private final String nomCombo;
    public CaseCombo(ControleurTable controleurTable, LinkedList<RangeVisible.ActionVisible> actionVisibles, String nomCombo) {
        super(actionVisibles);
        this.setPreferredSize(new Dimension(60, 40));

        this.controleur = controleurTable;
        this.nomCombo = nomCombo;

        JLabel labelCombo = new JLabel(nomCombo);
        labelCombo.setBounds(7, 5, 30, 15);
        Font font = new Font(labelCombo.getFont().getName(), Font.BOLD, 13);
        labelCombo.setFont(font);
        this.add(labelCombo);
        labelCombo.setForeground(Color.white);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int positionX = 0;
        int totalWidth = 0; // Initialisez la somme des largeurs à zéro

        for (int i = actionVisibles.size() - 1; i >= 0; i--) {
            RangeVisible.ActionVisible actionVisible = actionVisibles.get(i);
            Color couleur = actionVisible.getCouleur();
            int largeurX = Math.round(actionVisible.getPourcentage() / 100 * this.getWidth());

            if (largeurX == 0) continue;

            // on s'assure la somme des largeurs calculées ne dépasse pas la largeur du composant
            if (totalWidth + largeurX > getWidth()) {
                largeurX = getWidth() - totalWidth;
            }

            int debutX = positionX;
            positionX += largeurX;
            totalWidth += largeurX;

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
        controleur.clickCombo(nomCombo);
    }

}
