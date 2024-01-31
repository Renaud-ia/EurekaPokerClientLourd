package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.table.RangeVisible;

import javax.swing.*;

public class CaseStatsCombo extends JPanel {
    public CaseStatsCombo(ControleurTable controleurTable, RangeVisible.ComboVisible comboVisible) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel labelCombo = new JLabel(comboVisible.getNom());
        this.add(labelCombo);

        for (RangeVisible.ActionVisible actionVisible : comboVisible.getActions()) {
            JLabel labelAction = new JLabel(actionVisible.getNom() + " : " + actionVisible.getPourcentage());
            this.add(labelAction);
        }

        JLabel labelEquite = new JLabel("Equité vs ranges adverses : " + comboVisible.getEquite());
        this.add(labelEquite);
    }
}
