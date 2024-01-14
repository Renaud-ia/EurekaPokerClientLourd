package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.RangeVisible;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class CaseCombo extends CaseColorisable {
    private final ControleurTable controleur;
    private final String nomCombo;
    public CaseCombo(ControleurTable controleurTable, LinkedList<RangeVisible.ActionVisible> actionVisibles, String nomCombo) {
        super(actionVisibles);

        this.controleur = controleurTable;
        this.nomCombo = nomCombo;

        JLabel labelCombo = new JLabel(nomCombo);
        labelCombo.setBounds(7, 2, 30, 20);
        Font font = new Font(labelCombo.getFont().getName(), Font.BOLD, 13);
        labelCombo.setFont(font);
        this.add(labelCombo);
        labelCombo.setForeground(Color.white);

        this.addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        controleur.clickCombo(nomCombo);
    }

    // pour debug
    @Override
    protected void paintComponent(Graphics g) {
        System.out.println("REMPLISSAGE DE COMBO : " + nomCombo);
        super.paintComponent(g);
    }

}
