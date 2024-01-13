package analyzor.vue.table;

import analyzor.controleur.ControleurTable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CadreConfigTable extends CadreBandeau {

       public CadreConfigTable(ControleurTable controleur) {
           super("Configuration");
           JLabel label = new JLabel("Modifier la table");

           this.add(label);
           this.addMouseListener(new MouseAdapter() {
               @Override
               public void mouseClicked(MouseEvent e) {
                   // Appel au contrôleur lorsque le JPanel est cliqué
                   controleur.clickGestionTable();
               }
           });

       }

}

