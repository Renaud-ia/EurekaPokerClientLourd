package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.basiques.Polices;
import analyzor.vue.donnees.table.RangeVisible;
import analyzor.vue.reutilisables.PanneauFonceArrondi;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.LinkedList;

public class BlocDesActions extends PanneauFonceArrondi {
    private final ControleurTable controleurTable;
    private final HashMap<CaseAction, Integer> mapIndexActions;
    protected boolean survole;
    public static final int MIN_HAUTEUR = 150;
    public static final int MIN_LARGEUR = 500;
    public BlocDesActions(ControleurTable controleurTable) {
        super();
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        this.setMinimumSize(new Dimension(MIN_LARGEUR, MIN_HAUTEUR));

        this.controleurTable = controleurTable;
        this.mapIndexActions = new HashMap<>();

    }

    public void construireActions(LinkedList<RangeVisible.ActionVisible> actionVisibles) {
        this.removeAll();
        for (int i = actionVisibles.size() - 1; i >= 0; i--) {
            RangeVisible.ActionVisible action = actionVisibles.get(i);
            CaseAction caseAction = new CaseAction(action);
            caseAction.setPreferredSize(new Dimension(MIN_LARGEUR / actionVisibles.size() - 10, MIN_HAUTEUR / 2));
            caseAction.repaint();
            this.add(caseAction);
            mapIndexActions.put(caseAction, i);
        }

        this.revalidate();
        this.repaint();
    }

    private class CaseAction extends JPanel implements MouseListener{
        private final static int MARGE_INTERNE = 10;
        private final RangeVisible.ActionVisible actionVisible;
        private final String nomAction;
        private final float pctAction;
        private CaseAction(RangeVisible.ActionVisible actionVisible) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setAlignmentX(CENTER_ALIGNMENT);
            EmptyBorder bordureInterne = new EmptyBorder(MARGE_INTERNE, MARGE_INTERNE, MARGE_INTERNE, MARGE_INTERNE);
            this.setBorder(bordureInterne);

            this.nomAction = actionVisible.getNom();
            this.actionVisible = actionVisible;
            this.pctAction = actionVisible.getPourcentage();

            JLabel labelAction = new JLabel(nomAction);
            labelAction.setFont(Polices.standard);
            labelAction.setForeground(Polices.BLANC_TERNE);
            this.add(labelAction);

            this.add(Box.createRigidArea(new Dimension(0, 10)));

            JLabel labelPct = new JLabel(String.format("%.2f", pctAction) + "%");
            labelPct.setFont(Polices.standard);
            labelPct.setForeground(Polices.BLANC_TERNE);
            this.add(labelPct);
            this.addMouseListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Color couleurAction = actionVisible.getCouleur(survole);
            g.setColor(couleurAction);
            g.fillRect(0, 0, getWidth(), this.getHeight());
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == this) {
                // il faut déterminer sur quelle couleur on a cliqué
                controleurTable.clickActionsStats(mapIndexActions.get(this));
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            EmptyBorder bordureInterne = new EmptyBorder(MARGE_INTERNE, MARGE_INTERNE, MARGE_INTERNE, MARGE_INTERNE);
            LineBorder bordureBlanche = new LineBorder(Polices.BLANC_CLAIR, 2);
            CompoundBorder bordureComposee = new CompoundBorder(bordureBlanche, bordureInterne);
            this.setBorder(bordureComposee);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.setCursor(Cursor.getDefaultCursor());
            EmptyBorder bordureInterne = new EmptyBorder(MARGE_INTERNE, MARGE_INTERNE, MARGE_INTERNE, MARGE_INTERNE);
            this.setBorder(bordureInterne);
        }
    }
}
