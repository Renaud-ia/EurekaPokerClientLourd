package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.InfosSolution;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CadreSolution extends CadreBandeau {
    private InfosSolution infosSolution;

    public CadreSolution(InfosSolution infosSolution, ControleurTable controleur) {
        super("Format");
        this.infosSolution = infosSolution;
        JLabel varianteLabel = new JLabel("Variante : ");
        JLabel nombreDeJoueursLabel = new JLabel("Nombre de joueurs : ");
        JLabel ko = new JLabel("KO : ");

        this.add(varianteLabel);
        this.add(nombreDeJoueursLabel);
        this.add(ko);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Appel au contrôleur lorsque le JPanel est cliqué
                controleur.clickSolution();
            }
        });

        System.out.println("cadre solution créé");

    }

}
