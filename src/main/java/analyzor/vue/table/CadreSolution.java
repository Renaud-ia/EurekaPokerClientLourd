package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.InfosSolution;
import analyzor.vue.reutilisables.CadreClassique;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CadreSolution extends CadreClassique {
    private InfosSolution infosSolution;

    public CadreSolution(String name, InfosSolution infosSolution, ControleurTable controleur) {
        super(name);
        this.infosSolution = infosSolution;
        JLabel varianteLabel = new JLabel("Variante : " + infosSolution.getVariante());
        JLabel nombreDeJoueursLabel = new JLabel("Nombre de joueurs : " + infosSolution.getNombreDeJoueurs());
        JLabel ko = new JLabel("KO : " + infosSolution.getBounty());

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

    }

}
