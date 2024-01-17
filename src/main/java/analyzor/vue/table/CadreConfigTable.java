package analyzor.vue.table;

import analyzor.controleur.ControleurTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CadreConfigTable extends CadreBandeau {
    private final ControleurTable controleurTable;

       public CadreConfigTable(ControleurTable controleur) {
           super("Configuration");
           setBorder(bordureBlanche);
           setPreferredSize(new Dimension(130, hauteur));

           this.controleurTable = controleur;
           JLabel label = new JLabel("Modifier la table");

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

