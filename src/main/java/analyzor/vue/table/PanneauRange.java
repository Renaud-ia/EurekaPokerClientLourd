package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.table.RangeVisible;
import analyzor.vue.reutilisables.PanneauFonceArrondi;
import analyzor.vue.reutilisables.PanneauFond;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

public class PanneauRange extends PanneauFonceArrondi {
    private final ControleurTable controleurTable;
    private final HashMap<String, CaseCombo> caseCombos;
    JPanel vueRange;
    public PanneauRange(ControleurTable controleurTable, RangeVisible rangeVisible) {
        super();
        MARGE_HORIZONTALE = 5;
        MARGE_VERTICALE = 5;

        this.controleurTable = controleurTable;
        caseCombos = new HashMap<>();

        construireVueRange(rangeVisible);
        construireVueMessage();
    }

    private void construireVueRange(RangeVisible rangeVisible) {
        vueRange = new JPanel();
        vueRange.setLayout(new GridLayout(13, 13, 0, 0));

        for (RangeVisible.ComboVisible comboVisible : rangeVisible.listeDesCombos()) {
            CaseCombo nouvelleCase = new CaseCombo(controleurTable, comboVisible.getNom());
            caseCombos.put(comboVisible.getNom(), nouvelleCase);
            vueRange.add(nouvelleCase);
        }

        this.add(vueRange);
    }

    private void construireVueMessage() {
    }

    public void actualiserCombo(RangeVisible.ComboVisible comboVisible) {
        CaseCombo caseCombo = caseCombos.get(comboVisible.getNom());
        if (caseCombo == null) throw new IllegalArgumentException("Combo non trouvé: " + comboVisible.getNom());
        caseCombo.setActionsVisibles(comboVisible.getActions());
    }

    public void actualiserMessage(String message) {
    }

    public void redimensionner(int largeurRange, int hauteurRange) {
        int largeurCombo = largeurRange / 13;
        int hauteurCombo = hauteurRange / 13;

        CaseCombo.setDimensions(largeurCombo, hauteurCombo);
        for (CaseCombo component : caseCombos.values()) {
            component.setPreferredSize(new Dimension(largeurCombo, hauteurCombo));
            component.revalidate();
            component.repaint();
        }
        vueRange.revalidate();
        vueRange.repaint();
        this.revalidate();
        this.repaint();
    }
}
