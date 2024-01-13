package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.InfosSolution;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class CadreSolution extends CadreBandeau {
    private final ControleurTable controleurTable;
    private InfosSolution infosSolution;
    private final LabelSelectionnable varianteLabel;
    private final LabelSelectionnable nombreDeJoueursLabel;
    private final LabelSelectionnable koLabel;

    public CadreSolution(InfosSolution infosSolution, ControleurTable controleur) {
        super("Format");
        this.controleurTable = controleur;
        this.infosSolution = infosSolution;
        varianteLabel = new LabelSelectionnable();
        nombreDeJoueursLabel = new LabelSelectionnable();
        koLabel = new LabelSelectionnable();

        this.add(varianteLabel);
        this.add(nombreDeJoueursLabel);
        this.add(koLabel);

        this.addMouseListener(this);

        actualiser();
    }

    public void actualiser() {
        varianteLabel.setText("Variante : " + infosSolution.getVariante());
        nombreDeJoueursLabel.setText("Nombre de joueurs : " + infosSolution.getNombreDeJoueurs());
        koLabel.setText("KO : " + infosSolution.getBounty());
        this.repaint();
        this.revalidate();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        controleurTable.clickSolution();

    }
}
