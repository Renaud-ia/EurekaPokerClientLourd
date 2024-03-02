package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.donnees.*;
import analyzor.vue.donnees.table.*;
import analyzor.vue.reutilisables.PanneauFond;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * vue qui permet de configurer la table, choisir les actions et voir les ranges
 * c'est un panneau qu'on va mettre dans la fenêtre principale
 * les éléments de la vue appellent eux-même le controleur (= plus simple à gérer)
 */
public class VueTable extends PanneauFond {
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
        JPanel panneauGlobal = new PanneauFond();
        panneauGlobal.setBackground(CouleursDeBase.FOND_FENETRE);
        panneauGlobal.setLayout(new BoxLayout(panneauGlobal, BoxLayout.Y_AXIS));
        panneauGlobal.setAlignmentX(Component.LEFT_ALIGNMENT);

        // d'abord le panneau haut, on garde la référence car on devra rajouter des éléments
        panneauHaut = new PanneauHaut();
        panneauHaut.setLayout(new FlowLayout(FlowLayout.LEFT));

        // todo : on a pas besoin de garder les refs s'ils connaissent le controleur
        cadreSolution = new CadreSolution(infosSolution, controleur);
        panneauHaut.add(cadreSolution);

        configTable = new CadreConfigTable(controleur);
        panneauHaut.add(configTable);

        panneauGlobal.add(panneauHaut);

        vueRange = new VueRange(rangeVisible, controleur);
        panneauGlobal.add(vueRange);

        this.add(panneauGlobal);
        this.revalidate();
        this.repaint();

        frameParent.add(this, BorderLayout.WEST);
        frameParent.repaint();
        frameParent.pack();
    }

    // méthodes publiques utilisées par le controleur pour modifier la vue

    public void ajouterSituation(DTOSituation nouvelleCase) {
        CadreSituation nouveauCadre;
        if (nouvelleCase instanceof DTOSituationTrouvee) {
            nouveauCadre = new CadreSituationTrouvee(controleur, (DTOSituationTrouvee) nouvelleCase);
        }
        else if (nouvelleCase instanceof DTOInfo) {
            nouveauCadre = new CadreInfo(controleur, (DTOInfo) nouvelleCase);
        }
        else if (nouvelleCase instanceof DTOLeaf) {
            nouveauCadre = new CadreLeaf(controleur, (DTOLeaf) nouvelleCase);
        }
        else throw new IllegalArgumentException("Type inconnu");

        referencesCadre.put(nouvelleCase, nouveauCadre);
        panneauHaut.add(nouveauCadre);
        this.revalidate();
        this.repaint();
    }

    public void supprimerSituation(DTOSituation situationSupprimee) {
        CadreSituation cadreSituationTrouvee = referencesCadre.get(situationSupprimee);
        if (cadreSituationTrouvee == null) {
            throw new IllegalArgumentException("Cadre situation non trouvé");
        }

        panneauHaut.remove(cadreSituationTrouvee);
        this.revalidate();
        this.repaint();
    }

    public void selectionnerAction(DTOSituation dtoSituation, int indexAction) {
        CadreSituation cadreSituation = referencesCadre.get(dtoSituation);
        if (cadreSituation == null) {
            throw new IllegalArgumentException("Cadre situation non trouvé");
        }
        if (!(cadreSituation instanceof CadreSituationTrouvee)) return;
        ((CadreSituationTrouvee) cadreSituation).setActionSelectionnee(indexAction);
    }

    public void deselectionnerAction(DTOSituation dtoSituation, int indexAction) {
        CadreSituation cadreSituation = referencesCadre.get(dtoSituation);
        if (cadreSituation == null) {
            throw new IllegalArgumentException("Cadre situation non trouvé");
        }
        if (!(cadreSituation instanceof CadreSituationTrouvee)) return;
        ((CadreSituationTrouvee) cadreSituation).setActionDeselectionnee(indexAction);
    }

    public void selectionnerSituation(DTOSituation caseSelectionnee) {
        // on déselectionne les autres situations
        CadreSituation cadreSelectionnee = referencesCadre.get(caseSelectionnee);
        for (CadreSituation cadre : referencesCadre.values()) {
            cadre.setSelectionnee(cadre == cadreSelectionnee);
        }
    }

    public void actualiserSolution() {
        cadreSolution.actualiser();
    }

    public void actualiserConfigTable() {
        configTable.actualiser();
    }

    // on actualise à la fois vueRange et vueCombo car c'est le même panneau
    public void actualiserVueRange() {
        vueRange.actualiserRange();
        frameParent.pack();
    }

    public void actualiserVueCombo() {
        vueRange.actualiserStats();
        frameParent.pack();
    }


    public void viderRange() {
        this.rangeVisible.reset();
        actualiserVueRange();
    }

    public void redimensionnerRange() {
        vueRange.redimensionner(frameParent.getWidth(), frameParent.getHeight());
    }
}
