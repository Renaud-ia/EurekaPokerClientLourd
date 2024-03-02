package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.Polices;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CadreConfigTable extends CadreBandeau {
    private final ControleurTable controleurTable;

       public CadreConfigTable(ControleurTable controleur) {
           super("Configuration");
           setPreferredSize(new Dimension(140, hauteur));

           this.controleurTable = controleur;
           JLabel label = new JLabel("Modifier la table");
           label.setForeground(Polices.BLANC_CASSE);

           this.add(label);
           this.addMouseListener(this);

       }

    @Override
    public void mouseClicked(MouseEvent e) {
           controleurTable.clickGestionTable();
    }

    // on ne fait rien mais au cas où le connecteur existe
    public void actualiser() {
    }
}

