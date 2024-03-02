package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.donnees.table.RangeVisible;
import analyzor.vue.reutilisables.PanneauFonceArrondi;
import analyzor.vue.reutilisables.PanneauFond;

import javax.swing.*;
import java.awt.*;

/**
 * gère l'affichage de la Range mais aussi du détail combo
 */
public class VueRange extends PanneauFond {
    private final RangeVisible rangeVisible;
    private final ControleurTable controleurTable;
    private JPanel panneauRange;
    private JPanel panneauActions;
    private JPanel panneauCombo;
    private JPanel panneauStats;
    public VueRange(RangeVisible rangeVisible, ControleurTable controleur) {
        this.rangeVisible = rangeVisible;
        this.controleurTable = controleur;
        this.setBackground(CouleursDeBase.FOND_FENETRE);

        construirePanneaux();
    }

    private void construirePanneaux() {
        this.setLayout(new FlowLayout());
        panneauRange = new PanneauRange();
        this.add(panneauRange);

        panneauStats = new JPanel();
        panneauStats.setLayout(new BoxLayout(panneauStats, BoxLayout.Y_AXIS));
        panneauStats.setBackground(CouleursDeBase.FOND_FENETRE);

        panneauActions = new JPanel();
        panneauActions.setBackground(CouleursDeBase.FOND_FENETRE);
        panneauActions.setLayout(new FlowLayout());
        panneauStats.add(panneauActions);

        panneauCombo = new JPanel();
        panneauCombo.setBackground(CouleursDeBase.FOND_FENETRE);
        panneauCombo.setLayout(new FlowLayout());
        panneauStats.add(panneauCombo);

        this.add(panneauStats);

        actualiser();
    }

    public void actualiser() {
        actualiserRange();
        actualiserStats();
    }

    public void actualiserStats() {
        panneauActions.removeAll();
        panneauCombo.removeAll();

        // on ne rajoute ce panneau que s'il y a une range
        if (!(rangeVisible.estVide())) {
            CaseAction caseAction = new CaseAction(controleurTable, rangeVisible.actionsGlobales);
            caseAction.repaint();
            panneauActions.add(caseAction);

            RangeVisible.ComboVisible comboVisible = rangeVisible.comboSelectionne();
            CaseStatsCombo caseCombo = new CaseStatsCombo(controleurTable, comboVisible);
            panneauCombo.add(caseCombo);

        }
        panneauActions.repaint();
    }

    public void actualiserRange() {
        panneauRange.removeAll();

        for (RangeVisible.ComboVisible comboVisible : rangeVisible.listeDesCombos()) {
            CaseCombo caseCombo = new CaseCombo(controleurTable, comboVisible.getActions(), comboVisible.getNom());
            caseCombo.repaint();
            panneauRange.add(caseCombo);
        }

        panneauRange.repaint();
    }
}
