package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.poker.RangeReelle;

import java.util.List;

public class CalculatriceEquite {
    private final int nSimuFlop;
    private final int nSimuTurn;
    private final int nSimuRiver;
    private final float pctRangeHero;
    private final float pctRangeVillain;
    private final int nPercentiles;
    public CalculatriceEquite(
            int nSimuFlop,
            int nSimuTurn,
            int nSimuRiver,
            float pctRangeHero,
            float pctRangeVillain,
            int nPercentiles
    ) {
        this.nSimuFlop = nSimuFlop;
        this.nSimuTurn = nSimuTurn;
        this.nSimuRiver = nSimuRiver;
        this.pctRangeHero = pctRangeHero;
        this.pctRangeVillain = pctRangeVillain;
        this.nPercentiles = nPercentiles;
    }

    private float equiteMainBoard(ComboReel combo, Board board, List<RangeReelle> rangesVillains) {
        //TODO
        return 0f;
    }
}
