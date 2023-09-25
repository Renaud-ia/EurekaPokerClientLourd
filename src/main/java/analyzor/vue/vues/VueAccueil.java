package analyzor.vue.vues;

import analyzor.controleur.ControleurAccueil;
import analyzor.vue.composants.BandeauScrollBar;
import analyzor.vue.composants.CadreMiniTable;
import analyzor.vue.composants.CadreSolution;
import analyzor.vue.donnees.InfosSolution;

import javax.swing.*;
import java.awt.*;

public class VueAccueil extends JPanel {
    private final float pctEcranRange = 0.75F;
    private final float pctEcranBandeau = 0.20F;
    private JPanel panneauHaut = new JPanel();
    private JScrollPane bandeau = new JScrollPane(panneauHaut);
    private CadreSolution cadreSolution;
    private final JFrame frameParent;
    private final ControleurAccueil controleur;
    public VueAccueil(JFrame frameParent, ControleurAccueil controleur) {
        super();
        this.controleur = controleur;
        this.frameParent = frameParent;
        setLayout(new BorderLayout());
        add(bandeau, BorderLayout.NORTH);
        frameParent.add(this);
    }

    public void afficherSolution(InfosSolution infosSolution) {
        CadreSolution cadreSolution = new CadreSolution("Solution", infosSolution, this.controleur);
        panneauHaut.add(cadreSolution);
        actualiserBandeau();
    }

    public void afficherTable() {
        CadreMiniTable cadreTable = new CadreMiniTable("Table", this.controleur);
        panneauHaut.add(cadreTable);
        actualiserBandeau();

    }

    public void ajouterAction() {

    }

    public void afficherRange() {

    }

    public void afficherDetail() {

    }

    public void afficherStatsHero() {

    }

    public void afficherActions() {

    }

    public void afficherBoard() {

    }

    public void actualiserBandeau() {
        panneauHaut.revalidate();
        panneauHaut.repaint();
        bandeau.setViewportView(panneauHaut);
    }

}
