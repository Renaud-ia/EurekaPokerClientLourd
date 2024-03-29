package analyzor.modele.arbre;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;
import analyzor.modele.poker.evaluation.MatriceEquite;

public class RecupRangeDynamique extends RecuperateurRange {
    public RecupRangeDynamique(FormatSolution formatSolution) {
        super(formatSolution);
    }

    
    public MatriceEquite recupererMatrices(Entree entree) {
        return null;
    }
}
