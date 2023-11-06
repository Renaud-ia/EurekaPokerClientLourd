package analyzor.modele.arbre.noeuds;

/**
 * postflop, la série d'actions ne suffit plus à décrire les situations
 * Il faut clusteriser par MatriceEquite (RANGE x BOARD vs RANGES ADVERSES)
 */
public class NoeudPostflop extends NoeudAction {
    private int idCluster;
}
