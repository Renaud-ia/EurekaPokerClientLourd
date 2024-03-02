package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.table.RangeVisible;
import analyzor.vue.reutilisables.PanneauFonceArrondi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.LinkedList;

public class CaseAction extends PanneauFonceArrondi implements MouseListener {
    private final ControleurTable controleurTable;
    private final HashMap<BlocAction, Integer> mapIndexActions;
    protected boolean survole;
    protected final LinkedList<RangeVisible.ActionVisible> actionVisibles;
    private final int hauteur = 120;
    private final int largeur = 350;
    public CaseAction(ControleurTable controleurTable, LinkedList<RangeVisible.ActionVisible> actionVisibles) {
        super();
        this.actionVisibles = actionVisibles;
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        this.setPreferredSize(new Dimension(largeur, hauteur));

        this.controleurTable = controleurTable;
        this.mapIndexActions = new HashMap<>();

        construireActions();

        this.repaint();
        this.revalidate();
    }

    private void construireActions() {
        for (int i = actionVisibles.size() - 1; i >= 0; i--) {
            RangeVisible.ActionVisible action = actionVisibles.get(i);
            BlocAction blocAction = new BlocAction(action);
            blocAction.setPreferredSize(new Dimension(largeur / actionVisibles.size() - 10, hauteur / 2));
            blocAction.repaint();
            this.add(blocAction);
            mapIndexActions.put(blocAction, i);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() instanceof BlocAction) {
            // il faut déterminer sur quelle couleur on a cliqué
            controleurTable.clickActionsStats(mapIndexActions.get((BlocAction) e.getSource()));
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

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private class BlocAction extends JPanel {
        private final RangeVisible.ActionVisible actionVisible;
        private final String nomAction;
        private final float pctAction;
        private BlocAction(RangeVisible.ActionVisible actionVisible) {
            this.setLayout(new BorderLayout());
            this.nomAction = actionVisible.getNom();
            this.actionVisible = actionVisible;
            this.pctAction = (float) Math.round(actionVisible.getPourcentage());

            JLabel labelAction = new JLabel(nomAction);
            labelAction.setForeground(Color.white);
            this.add(labelAction, BorderLayout.NORTH);
            JLabel labelPct = new JLabel(pctAction + "%");
            labelPct.setForeground(Color.white);
            this.add(labelPct, BorderLayout.SOUTH);
            this.addMouseListener(CaseAction.this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Color couleurAction = actionVisible.getCouleur(survole);
            g.setColor(couleurAction);
            g.fillRect(0, 0, getWidth(), this.getHeight());
        }
    }
}
