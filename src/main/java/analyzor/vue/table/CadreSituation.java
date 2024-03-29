package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.donnees.table.DTOSituation;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class CadreSituation extends CadreBandeau {
    protected final ControleurTable controleurTable;
    protected final DTOSituation dtoSituationTrouvee;

    public CadreSituation(ControleurTable controleur, DTOSituation nouvelleCase) {
        super(nouvelleCase.getNom());
        setPreferredSize(new Dimension(130, hauteur));
        addMouseListener(this);

        this.controleurTable = controleur;
        this.dtoSituationTrouvee = nouvelleCase;
    }


    
    public void setSelectionnee(boolean selectionne) {
        if (selectionne) couleurFond = CouleursDeBase.PANNEAU_SELECTIONNE;
        else couleurFond = CouleursDeBase.PANNEAU_FONCE;
        this.selectionne = selectionne;
        repaint();
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
