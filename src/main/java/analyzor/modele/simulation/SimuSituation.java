package analyzor.modele.simulation;

import java.util.List;

public class SimuSituation {
    // stocke la liste des situations gardées en mémoire par la table
    private int index;
    private Joueur joueur;
    private boolean fixe = false;
    private List<SimuAction> actions;
}
