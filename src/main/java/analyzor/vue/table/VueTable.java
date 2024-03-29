package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.donnees.*;
import analyzor.vue.donnees.table.*;
import analyzor.vue.reutilisables.NombreModifiable;
import analyzor.vue.reutilisables.PanneauFond;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;


public class VueTable extends PanneauFond {
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
        
        this.controleur = controleur;
        this.frameParent = frameParent;

        
        this.infosSolution = infosSolution;
        this.situations = situations;
        this.rangeVisible = rangeVisible;

        referencesCadre = new HashMap<>();

        initialiserVue();
    }

    private void initialiserVue() {
        
        JPanel panneauGlobal = new PanneauFond();
        panneauGlobal.setBackground(CouleursDeBase.FOND_FENETRE);
        panneauGlobal.setLayout(new BoxLayout(panneauGlobal, BoxLayout.Y_AXIS));
        panneauGlobal.setAlignmentX(Component.LEFT_ALIGNMENT);

        
        panneauHaut = new PanneauHaut();

        
        cadreSolution = new CadreSolution(infosSolution, controleur);
        panneauHaut.add(cadreSolution);

        configTable = new CadreConfigTable(controleur);
        panneauHaut.add(configTable);

        panneauGlobal.add(panneauHaut);

        vueRange = new VueRange(rangeVisible, controleur);
        panneauGlobal.add(vueRange);

        this.add(panneauGlobal);

        frameParent.add(this, BorderLayout.WEST);
        frameParent.pack();
    }

    

    public void ajouterSituation(DTOSituation nouvelleCase) {
        CadreSituation nouveauCadre = switch (nouvelleCase) {
            case DTOSituationTrouvee dtoSituationTrouvee -> new CadreSituationTrouvee(controleur, dtoSituationTrouvee);
            case DTOInfo dtoInfo -> new CadreInfo(controleur, dtoInfo);
            case DTOLeaf dtoLeaf -> new CadreLeaf(controleur, dtoLeaf);
            case null, default -> throw new IllegalArgumentException("Type inconnu");
        };

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
        
        CadreSituation cadreSelectionnee = referencesCadre.get(caseSelectionnee);
        for (CadreSituation cadre : referencesCadre.values()) {
            cadre.setSelectionnee(cadre == cadreSelectionnee);
        }
    }

    public void actualiserSolution() {
        cadreSolution.actualiser();
    }

    public void actualiserConfigTable(float stackMoyen) {
        configTable.actualiser(stackMoyen);
    }


    public void actualiserVueRange() {
        vueRange.actualiserRange();
        vueRange.actualiserActions();
    }

    public void actualiserVueCombo() {
        vueRange.actualiserStats();
    }


    public void viderRange() {
        this.rangeVisible.reset();
        actualiserVueRange();
    }

    public void redimensionnerRange() {
        vueRange.redimensionner(frameParent.getWidth(), frameParent.getHeight());
    }

    public NombreModifiable getCaseComboStats() {
        return vueRange.getCaseComboStats();
    }
}
