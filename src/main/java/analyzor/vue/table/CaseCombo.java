package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.RangeVisible;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class CaseCombo extends CaseColorisable {
    private final ControleurTable controleur;
    private final String nomCombo;
    public CaseCombo(ControleurTable controleurTable, LinkedList<RangeVisible.ActionVisible> actionVisibles, String nomCombo) {
        super(actionVisibles);

        this.controleur = controleurTable;
        this.nomCombo = nomCombo;

        JLabel labelTest = new JLabel(nomCombo);
        this.add(labelTest);

        this.addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        controleur.clickCombo(nomCombo);
    }

}
