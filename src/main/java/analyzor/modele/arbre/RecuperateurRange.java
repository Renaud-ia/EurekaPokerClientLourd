package analyzor.modele.arbre;

import analyzor.modele.parties.Entree;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.RangeDenombrable;

import java.util.List;

/**
 * va récupérer les ranges à partir d'un échantillon d'entrée
 */
public class RecuperateurRange {
    private RangeDenombrable rangeHero;
    private List<RangeDenombrable> rangesVillains;
    private Board board;

    // à partir d'un échantillon va récupérer les ranges moyennes
    public RecuperateurRange(List<Entree> entrees) {

    }

    public RangeDenombrable getRangeHero() {
        return rangeHero;
    }
}
