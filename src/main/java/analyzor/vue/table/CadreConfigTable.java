package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.Polices;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CadreConfigTable extends CadreBandeau {
    final JLabel labelStackMoyen;
    private final ControleurTable controleurTable;

       public CadreConfigTable(ControleurTable controleur) {
           super("Table");
           setPreferredSize(new Dimension(140, hauteur));

           this.controleurTable = controleur;

           labelStackMoyen = new JLabel();
           labelStackMoyen.setForeground(Polices.BLANC_CASSE);
           labelStackMoyen.setText("Stack moy. : - ");
           this.add(labelStackMoyen);

           this.add(Box.createRigidArea(new Dimension(0, 10)));

           this.addMouseListener(this);

       }

    @Override
    public void mouseClicked(MouseEvent e) {
           controleurTable.clickGestionTable();
    }

    // on ne fait rien mais au cas où le connecteur existe
    public void actualiser(float stackMoyen) {
           labelStackMoyen.setText("Stack moy. : " + String.format("%.0f", stackMoyen) + "bb");
    }
}

