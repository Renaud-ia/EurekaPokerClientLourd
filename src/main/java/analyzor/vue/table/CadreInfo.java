package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.DTOInfo;

import javax.swing.*;

public class CadreInfo extends CadreSituation {

    public CadreInfo(ControleurTable controleur, DTOInfo nouvelleCase) {
        super(controleur, nouvelleCase);
        JLabel label = new JLabel(nouvelleCase.getMessage());
        this.add(label);
    }
}
