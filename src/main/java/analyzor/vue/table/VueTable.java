package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.DTOSituation;
import analyzor.vue.donnees.RangeVisible;
import analyzor.vue.reutilisables.BandeauScrollBar;
import analyzor.vue.donnees.InfosSolution;

import javax.swing.*;
import java.util.LinkedList;

/**
 * vue qui permet de configurer la table, choisir les actions et voir les ranges
 * c'est un panneau qu'on va mettre dans la fenêtre principale
 */
public class VueTable extends JPanel {
    private final float pctEcranRange = 0.75F;
    private final float pctEcranBandeau = 0.20F;
    private JPanel panneauHaut = new JPanel();
    private JScrollPane bandeau = new BandeauScrollBar(panneauHaut);
    private final JFrame frameParent;
    private final ControleurTable controleur;
    private final InfosSolution infosSolution;
    private final LinkedList<DTOSituation> situations;
    private final RangeVisible rangeVisible;
    public VueTable(JFrame frameParent, ControleurTable controleur,
                    InfosSolution infosSolution, LinkedList<DTOSituation> situations, RangeVisible rangeVisible) {
        super();
        this.controleur = controleur;
        this.frameParent = frameParent;

        this.infosSolution = infosSolution;
        this.situations = situations;
        this.rangeVisible = rangeVisible;

        initialiserVue();
    }

    private void initialiserVue() {
        // on crée les panneaux
    }

    // méthodes publiques utilisées par le controleur pour modifier la vue
    public void selectionnerAction(DTOSituation dtoSituation, int indexAction) {
    }

    public void situationSelectionnee(DTOSituation caseSelectionnee) {
    }

    public void ajouterSituation(DTOSituation nouvelleCase) {
    }

    public void supprimerSituation(DTOSituation situationSupprimee) {
    }

    public void actualiserSolution() {
    }

    public void actualiserConfigTable() {
    }

    public void actualiserVueRange() {
    }

    public void deselectionnerAction(DTOSituation dtoSituation, int indexAction) {
    }
}
