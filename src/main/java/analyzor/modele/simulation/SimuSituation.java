package analyzor.modele.simulation;

import analyzor.modele.poker.RangeIso;

import java.util.HashMap;
import java.util.List;

public class SimuSituation {
    // stocke la liste des situations gardées en mémoire par la table
    private int index;
    private JoueurSimulation joueur;
    private boolean fixe = false;
    private List<SimuAction> actions;
    private HashMap<SimuAction, RangeIso> rangesActions;
    private HashMap<SimuAction, RangeCondensee> rangesVisibles;
    private HashMap<SimuAction, SimuSituation> prochainesSituations;

    public HashMap<SimuAction, RangeCondensee> getRanges() {
        return rangesVisibles;
    }
}
