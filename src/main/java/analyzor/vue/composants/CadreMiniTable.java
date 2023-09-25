package analyzor.vue.composants;

import analyzor.controleur.ControleurAccueil;
import analyzor.vue.donnees.InfosSolution;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CadreMiniTable extends CadreClassique {

       public CadreMiniTable(String name, ControleurAccueil controleur) {
           super(name);
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

