package analyzor.modele.equilibrage;

import analyzor.modele.poker.evaluation.EquiteFuture;

/**
 * noeud parent de l'arbre d'équilibrage
 * permet de tenir compte de la proximité d'autres combos pour l'équilibrage
 */
public class NoeudEquilibrage {
    private final RegressionEquilibrage regression;
    private NoeudEquilibrage parent;
    private float pNoeud;
    private int[] strategieActuelle;
    private float[] equiteMoyenne;
    public NoeudEquilibrage(RegressionEquilibrage regression, int nActions) {
        this.regression = regression;
        this.strategieActuelle = new int[nActions];
    }

    public void testerChangement(int indexAction, int increment) {
        // on va répercuter sur la régression
    }
}
