package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.DTOSituationTrouvee;
import analyzor.vue.donnees.InfosAction;

import java.awt.*;
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
            this.add(labelAction);
            labelsAction.add(labelAction);
        }
    }

    public void setActionSelectionnee(int indexAction) {
        // on déselectionne toutes les autres actions
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
        if (e.getSource() instanceof LabelSelectionnable) {
            LabelSelectionnable labelClique = (LabelSelectionnable) e.getSource();
            int indexAction = labelsAction.indexOf(labelClique);
            if (indexAction == -1) throw new IllegalArgumentException("Le composant cliqué n'est pas référencé");
            controleurTable.clickAction((DTOSituationTrouvee) dtoSituationTrouvee, indexAction);
        }

        else if (e.getSource() == this) {
            controleurTable.clickSituation(dtoSituationTrouvee);
        }

    }

}
