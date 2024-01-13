package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.DTOSituation;
import analyzor.vue.donnees.InfosAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;

public class CadreSituation extends CadreBandeau {
    private final ControleurTable controleurTable;
    private final DTOSituation dtoSituation;
    private final LinkedList<LabelSelectionnable> labelsAction;
    public CadreSituation(ControleurTable controleur, DTOSituation nouvelleCase) {
        super(nouvelleCase.getNom());
        this.controleurTable = controleur;
        this.dtoSituation = nouvelleCase;

        addMouseListener(this);

        labelsAction = new LinkedList<>();

        construireActions();
    }

    private void construireActions() {
        for (InfosAction action : dtoSituation.getActions()) {
            LabelSelectionnable labelAction = new LabelSelectionnable(action.getNom());
            labelAction.addMouseListener(this);
            this.add(labelAction);
            labelsAction.add(labelAction);
        }
    }

    public void setActionSelectionnee(int indexAction) {
        LabelSelectionnable label = labelsAction.get(indexAction);
        if (label == null) throw new IllegalArgumentException("Label action non trouvé");
        label.selectionner();
    }

    public void setActionDeselectionnee(int indexAction) {
        LabelSelectionnable label = labelsAction.get(indexAction);
        if (label == null) throw new IllegalArgumentException("Label action non trouvé");
        label.deselectionner();
    }

    /**
     * méthode pour signifier que la situation a été sélectionnée
     */
    public void setSelectionnee() {
        // todo il faudrait changer la couleur du cadre (ou quelque chose du genre)
        // on ne déselectionne pas l'action
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() instanceof LabelSelectionnable) {
            LabelSelectionnable labelClique = (LabelSelectionnable) e.getSource();
            int indexAction = labelsAction.indexOf(labelClique);
            if (indexAction == -1) throw new IllegalArgumentException("Le composant cliqué n'est pas référencé");
            controleurTable.clickAction(dtoSituation, indexAction);
        }

        else if (e.getSource() == this) {
            controleurTable.clickSituation(dtoSituation);
        }

    }

}
