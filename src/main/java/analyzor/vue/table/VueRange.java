package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.basiques.Images;
import analyzor.vue.basiques.Polices;
import analyzor.vue.donnees.table.RangeVisible;
import analyzor.vue.reutilisables.NombreModifiable;
import analyzor.vue.reutilisables.PanneauFond;
import jakarta.persistence.criteria.CriteriaBuilder;

import javax.swing.*;
import java.awt.*;

/**
 * gère l'affichage de la Range mais aussi du détail combo
 */
public class VueRange extends PanneauFond {
    private final RangeVisible rangeVisible;
    private final ControleurTable controleurTable;
    private PanneauRange panneauRange;
    private JPanel panneauActions;
    private JPanel panneauCombo;
    private JPanel panneauStats;
    private CaseStatsCombo caseCombo;
    private int largeurRange = 500;
    private int hauteurRange = 500;
    TexteRange texteRange;
    public VueRange(RangeVisible rangeVisible, ControleurTable controleur) {
        this.rangeVisible = rangeVisible;
        this.controleurTable = controleur;
        this.setBackground(CouleursDeBase.FOND_FENETRE);

        construirePanneaux();
    }

    private void construirePanneaux() {
        this.setLayout(new FlowLayout());
        JPanel panneauCompletRange = new JPanel();
        panneauCompletRange.setLayout(new BoxLayout(panneauCompletRange, BoxLayout.Y_AXIS));
        panneauRange = new PanneauRange(controleurTable, rangeVisible);
        panneauCompletRange.add(panneauRange);
        panneauCompletRange.add(Box.createRigidArea(new Dimension(0, 5)));

        texteRange = new TexteRange(rangeVisible, largeurRange);
        panneauCompletRange.add(texteRange);
        panneauCompletRange.add(Box.createRigidArea(new Dimension(0, 5)));

        this.add(panneauCompletRange);

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
            System.out.println("RANGE VIDE");
            BlocDesActions blocDesActions = new BlocDesActions(controleurTable, rangeVisible.actionsGlobales);
            blocDesActions.repaint();
            panneauActions.add(blocDesActions);

            RangeVisible.ComboVisible comboVisible = rangeVisible.comboSelectionne();
            caseCombo = new CaseStatsCombo(controleurTable, comboVisible);
            panneauCombo.add(caseCombo);
        }

        panneauActions.repaint();
    }

    public void actualiserRange() {
        for (RangeVisible.ComboVisible comboVisible : rangeVisible.listeDesCombos()) {
            panneauRange.actualiserCombo(comboVisible);
            panneauRange.actualiserMessage(rangeVisible.getMessage());
        }

        texteRange.setTexte();
    }

    public void redimensionner(int width, int height) {
        largeurRange = width - BlocDesActions.MIN_LARGEUR - 70;
        hauteurRange = height - CadreBandeau.hauteur - TexteRange.HAUTEUR_BANDEAU - 120;
        panneauRange.redimensionner(largeurRange, hauteurRange);

        texteRange.actualiser(largeurRange);
    }


    public NombreModifiable getCaseComboStats() {
        return caseCombo;
    }
}
