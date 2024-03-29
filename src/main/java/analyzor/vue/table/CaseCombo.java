package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.CouleursActions;
import analyzor.vue.basiques.Polices;
import analyzor.vue.donnees.table.RangeVisible;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class CaseCombo extends CaseColorisable {
    private final ControleurTable controleur;
    private final String nomCombo;
    private static int largeur = 50;
    private static int hauteur = 50;

    public static void setDimensions(int i, int i1) {
        largeur = i;
        hauteur = i1;
    }

    public CaseCombo(ControleurTable controleurTable,
                     String nomCombo) {
        super();

        this.setPreferredSize(new Dimension(largeur, hauteur));

        this.controleur = controleurTable;
        this.nomCombo = nomCombo;

        JLabel labelCombo = new JLabel(nomCombo);
        labelCombo.setBounds(7, 5, 30, 15);
        labelCombo.setFont(Polices.standard);
        labelCombo.setForeground(Polices.BLANC_CLAIR);
        this.add(labelCombo);

    }

    public void setActionsVisibles(LinkedList<RangeVisible.ActionVisible> actionVisibles) {
        this.actionVisibles = actionVisibles;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int positionX = 0;
        int totalWidth = 0;

        for (int i = actionVisibles.size() - 1; i >= 0; i--) {
            RangeVisible.ActionVisible actionVisible = actionVisibles.get(i);
            Color couleur = actionVisible.getCouleur(survole);
            int largeurX = Math.round(actionVisible.getPourcentage() / 100 * largeur);

            if (largeurX == 0) continue;


            if (totalWidth + largeurX > largeur) {
                largeurX = largeur - totalWidth;
            }

            int debutX = positionX;
            positionX += largeurX;
            totalWidth += largeurX;

            g.setColor(couleur);
            g.fillRect(debutX, 0, positionX, hauteur);

        }

        if (positionX < largeur) {
            if (survole) g.setColor(CouleursActions.CASE_SURVOLEE);
            else g.setColor(CouleursActions.ACTION_NON_DEFINIE);
            g.fillRect(positionX, 0, largeur, hauteur);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        controleur.clickCombo(nomCombo);
    }
}
