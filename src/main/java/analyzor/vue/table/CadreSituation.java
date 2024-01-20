package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.DTOSituation;
import analyzor.vue.donnees.DTOSituationTrouvee;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class CadreSituation extends CadreBandeau {
    protected final ControleurTable controleurTable;
    protected final DTOSituation dtoSituationTrouvee;
    public CadreSituation(ControleurTable controleur, DTOSituation nouvelleCase) {
        super(nouvelleCase.getNom());
        setBorder(bordureBlanche);
        setPreferredSize(new Dimension(110, hauteur));
        addMouseListener(this);

        this.controleurTable = controleur;
        this.dtoSituationTrouvee = nouvelleCase;
    }


    /**
     * méthode pour signifier que la situation a été sélectionnée
     */
    public void setSelectionnee(boolean selectionne) {
        // todo il faut déselectionner l'action
        if (selectionne) {
            setBorder(bordureSurlignee);
        }
        else {
            setBorder(bordureBlanche);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() instanceof LabelSelectionnable) {
            return;
        }

        else if (e.getSource() == this) {
            controleurTable.clickSituation(dtoSituationTrouvee);
        }

    }


}
