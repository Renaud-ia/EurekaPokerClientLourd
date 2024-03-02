package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.Polices;
import analyzor.vue.donnees.table.RangeVisible;
import analyzor.vue.reutilisables.PanneauFonceArrondi;

import javax.swing.*;
import java.awt.*;

public class CaseStatsCombo extends PanneauFonceArrondi {
    public CaseStatsCombo(ControleurTable controleurTable, RangeVisible.ComboVisible comboVisible) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel labelCombo = new JLabel("Combo : " + comboVisible.getNom());
        labelCombo.setFont(Polices.standard);
        labelCombo.setForeground(Polices.BLANC_CLAIR);
        this.add(labelCombo);
        this.add(Box.createRigidArea(new Dimension(0, 10)));

        for (RangeVisible.ActionVisible actionVisible : comboVisible.getActions()) {
            JLabel labelAction = new JLabel(actionVisible.getNom() + " : " + actionVisible.getPourcentage());
            labelAction.setFont(Polices.standard);
            labelAction.setForeground(Polices.BLANC_TERNE);
            this.add(labelAction);
        }
        this.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel labelEquite = new JLabel("Equité : " + comboVisible.getEquite());
        labelEquite.setFont(Polices.standard);
        labelEquite.setForeground(Polices.BLANC_TERNE);
        this.add(labelEquite);
    }
}
