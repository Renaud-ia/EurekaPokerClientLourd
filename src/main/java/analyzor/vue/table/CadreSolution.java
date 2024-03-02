package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.Polices;
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
        setPreferredSize(new Dimension(150, hauteur));

        this.controleurTable = controleur;
        this.infosSolution = infosSolution;
        varianteLabel = new LabelSelectionnable();
        varianteLabel.setForeground(Polices.BLANC_CASSE);
        nombreDeJoueursLabel = new LabelSelectionnable();
        nombreDeJoueursLabel.setForeground(Polices.BLANC_CASSE);
        koLabel = new LabelSelectionnable();
        koLabel.setForeground(Polices.BLANC_CASSE);

        this.add(varianteLabel);
        this.add(nombreDeJoueursLabel);
        this.add(koLabel);

        this.addMouseListener(this);

        actualiser();
    }

    public void actualiser() {
        varianteLabel.setText("Variante : " + infosSolution.getVariante());
        nombreDeJoueursLabel.setText("Joueurs : " + infosSolution.getNombreDeJoueurs());
        koLabel.setText("KO : " + infosSolution.getBounty());
        this.repaint();
        this.revalidate();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        controleurTable.clickSolution();
    }
}
