package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.donnees.DTOSituation;
import analyzor.vue.donnees.RangeVisible;
import analyzor.vue.donnees.InfosSolution;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * vue qui permet de configurer la table, choisir les actions et voir les ranges
 * c'est un panneau qu'on va mettre dans la fenêtre principale
 * les éléments de la vue appellent eux-même le controleur (= plus simple à gérer)
 */
public class VueTable extends JPanel {
    private final float pctEcranRange = 0.75F;
    private final float pctEcranBandeau = 0.20F;
    private PanneauHaut panneauHaut;
    private CadreSolution cadreSolution;
    private CadreConfigTable configTable;
    private VueRange vueRange;
    private final JFrame frameParent;
    private final ControleurTable controleur;
    private final InfosSolution infosSolution;
    private final LinkedList<DTOSituation> situations;
    private final RangeVisible rangeVisible;
    private final HashMap<DTOSituation, CadreSituation> referencesCadre;
    public VueTable(JFrame frameParent, ControleurTable controleur,
                    InfosSolution infosSolution, LinkedList<DTOSituation> situations, RangeVisible rangeVisible) {
        super();
        // initialisation des parents
        this.controleur = controleur;
        this.frameParent = frameParent;

        // initialisation des DTO
        this.infosSolution = infosSolution;
        this.situations = situations;
        this.rangeVisible = rangeVisible;

        referencesCadre = new HashMap<>();

        initialiserVue();
    }

    private void initialiserVue() {
        // on crée les panneaux
        JPanel panneauGlobal = new JPanel();
        panneauGlobal.setLayout(new BoxLayout(panneauGlobal, BoxLayout.Y_AXIS));

        // d'abord le panneau haut, on garde la référence car on devra rajouter des éléments
        panneauHaut = new PanneauHaut();
        panneauHaut.setLayout(new FlowLayout(FlowLayout.LEFT));

        // todo : on a pas besoin de garder les refs s'ils connaissent le controleur
        cadreSolution = new CadreSolution(infosSolution, controleur);
        panneauHaut.add(cadreSolution);

        configTable = new CadreConfigTable(controleur);
        panneauHaut.add(configTable);

        panneauGlobal.add(panneauHaut, BorderLayout.CENTER);

        vueRange = new VueRange(rangeVisible, controleur);
        panneauGlobal.add(vueRange, BorderLayout.CENTER);

        this.add(panneauGlobal);
        this.revalidate();
        this.repaint();

        frameParent.add(this);
        frameParent.repaint();
        frameParent.revalidate();
    }

    // méthodes publiques utilisées par le controleur pour modifier la vue

    public void ajouterSituation(DTOSituation nouvelleCase) {
        CadreSituation cadreSituation = new CadreSituation(controleur, nouvelleCase);
        referencesCadre.put(nouvelleCase, cadreSituation);
        panneauHaut.add(cadreSituation);
        this.revalidate();
        this.repaint();
    }

    public void supprimerSituation(DTOSituation situationSupprimee) {
        CadreSituation cadreSituation = referencesCadre.get(situationSupprimee);
        if (cadreSituation == null) {
            throw new IllegalArgumentException("Cadre situation non trouvé");
        }

        panneauHaut.remove(cadreSituation);
        this.revalidate();
        this.repaint();
    }

    public void selectionnerAction(DTOSituation dtoSituation, int indexAction) {
        CadreSituation cadreSituation = referencesCadre.get(dtoSituation);
        if (cadreSituation == null) {
            throw new IllegalArgumentException("Cadre situation non trouvé");
        }
        cadreSituation.setActionSelectionnee(indexAction);
    }

    public void deselectionnerAction(DTOSituation dtoSituation, int indexAction) {
        CadreSituation cadreSituation = referencesCadre.get(dtoSituation);
        if (cadreSituation == null) {
            throw new IllegalArgumentException("Cadre situation non trouvé");
        }
        cadreSituation.setActionDeselectionnee(indexAction);
    }

    public void selectionnerSituation(DTOSituation caseSelectionnee) {
        CadreSituation cadreSituation = referencesCadre.get(caseSelectionnee);
        if (cadreSituation == null) {
            throw new IllegalArgumentException("Cadre situation non trouvé");
        }
        cadreSituation.setSelectionnee();
    }

    public void actualiserSolution() {
        cadreSolution.actualiser();
    }

    public void actualiserConfigTable() {
        configTable.actualiser();
    }

    // on actualise à la fois vueRange et vueCombo car c'est le même panneau
    public void actualiserVueRange() {
        vueRange.actualiser();
    }

}
