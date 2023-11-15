package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;

/**
 * postflop, la série d'actions ne suffit plus à décrire les situations
 * Il faut clusteriser par MatriceEquite (RANGE x BOARD vs RANGES ADVERSES)
 */
public class NoeudPostflop extends NoeudAction {
    private int idCluster;

    public NoeudPostflop(FormatSolution formatSolution, Long idNoeudTheorique, float stackEffectif, float pot, float potBounty) {
        super(formatSolution, idNoeudTheorique, stackEffectif, pot, potBounty);
    }
}
