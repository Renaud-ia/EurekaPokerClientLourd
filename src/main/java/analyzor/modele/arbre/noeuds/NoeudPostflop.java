package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;

/**
 * postflop, la série d'actions ne suffit plus à décrire les situations
 * Il faut clusteriser par MatriceEquite (RANGE x BOARD vs RANGES ADVERSES)
 */
public class NoeudPostflop extends NoeudAction {
    // pour hibernate
    public NoeudPostflop() {super();}
    private int idCluster;

    public NoeudPostflop(NoeudSituation noeudSituation, long idNoeudTheorique) {
        super(noeudSituation, idNoeudTheorique);
    }
}
