package analyzor.vue.composants;

import analyzor.controleur.ControleurAccueil;
import analyzor.vue.donnees.InfosSolution;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CadreSolution extends CadreClassique {
    private InfosSolution infosSolution;

    public CadreSolution(String name, InfosSolution infosSolution, ControleurAccueil controleur) {
        super(name);
        this.infosSolution = infosSolution;
        JLabel varianteLabel = new JLabel("Variante : " + infosSolution.getVariante());
        JLabel stackLabel = new JLabel("Stack : " + infosSolution.getStack());
        JLabel nombreDeJoueursLabel = new JLabel("Nombre de joueurs : " + infosSolution.getNombreDeJoueurs());

        this.add(varianteLabel);
        this.add(stackLabel);
        this.add(nombreDeJoueursLabel);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Appel au contrôleur lorsque le JPanel est cliqué
                controleur.clickSolution();
            }
        });

    }

}
