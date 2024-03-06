package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.Polices;
import analyzor.vue.donnees.table.RangeVisible;
import analyzor.vue.reutilisables.NombreModifiable;
import analyzor.vue.reutilisables.PanneauFonceArrondi;

import javax.naming.Name;
import javax.swing.*;
import java.awt.*;

public class CaseStatsCombo extends PanneauFonceArrondi implements NombreModifiable {
    private static final int MIN_HAUTEUR = 200;
    private final JLabel labelEquite;
    public CaseStatsCombo(ControleurTable controleurTable, RangeVisible.ComboVisible comboVisible) {
        super();
        MARGE_VERTICALE = 20;
        MARGE_HORIZONTALE = 20;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel labelCombo = new JLabel("Combo : " + comboVisible.getNom());
        labelCombo.setFont(Polices.titre);
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

        labelEquite = new JLabel();
        labelEquite.setText("Equité : calcul en cours...");
        labelEquite.setFont(Polices.standard);
        labelEquite.setForeground(Polices.BLANC_TERNE);
        this.add(labelEquite);

        this.setPreferredSize(new Dimension(BlocDesActions.MIN_LARGEUR, MIN_HAUTEUR));
    }

    @Override
    public void modifierNombre(float nombre) {
        labelEquite.setText("Equité : " + String.format("%.2f", nombre * 100) + "%");
    }
}
