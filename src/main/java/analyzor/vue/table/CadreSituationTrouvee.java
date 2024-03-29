package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.Polices;
import analyzor.vue.donnees.table.DTOSituationTrouvee;
import analyzor.vue.donnees.table.InfosAction;

import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class CadreSituationTrouvee extends CadreSituation {
    private final LinkedList<LabelSelectionnable> labelsAction;
    public CadreSituationTrouvee(ControleurTable controleur, DTOSituationTrouvee nouvelleCase) {
        super(controleur, nouvelleCase);

        labelsAction = new LinkedList<>();

        construireActions();
    }

    private void construireActions() {
        for (InfosAction action : ((DTOSituationTrouvee) dtoSituationTrouvee).getActions()) {
            LabelSelectionnable labelAction = new LabelSelectionnable(action.getNom());
            labelAction.addMouseListener(this);
            labelAction.setFont(Polices.standard);
            labelAction.setForeground(Polices.BLANC_TERNE);
            this.add(labelAction);
            labelsAction.add(labelAction);
        }
    }

    public void setActionSelectionnee(int indexAction) {
        
        for (int i = 0; i < labelsAction.size(); i++) {
            LabelSelectionnable labelSelectionnable = labelsAction.get(i);
            if (i == indexAction) labelSelectionnable.selectionner();
            else labelSelectionnable.deselectionner();
        }
        this.repaint();
    }

    public void setActionDeselectionnee(int indexAction) {
        LabelSelectionnable label = labelsAction.get(indexAction);
        if (label == null) throw new IllegalArgumentException("Label action non trouvé");
        label.deselectionner();
        this.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() instanceof LabelSelectionnable labelClique) {
            int indexAction = labelsAction.indexOf(labelClique);
            if (indexAction == -1) throw new IllegalArgumentException("Le composant cliqué n'est pas référencé");
            controleurTable.clickAction((DTOSituationTrouvee) dtoSituationTrouvee, indexAction);
        }

        else if (e.getSource() == this) {
            controleurTable.clickSituation(dtoSituationTrouvee);
        }

    }

}
