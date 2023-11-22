package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.RangeDenombrable;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.poker.RangeReelle;

import java.util.ArrayList;
import java.util.List;

public class OppositionRange {
    private RangeDenombrable rangeHero;
    // ne sert qu'au calcul d'équité des combos pour clustering
    private List<RangeReelle> rangesVillains;

    public void setRangeHero(RangeIso rangeHero) {
        this.rangeHero = rangeHero;
        this.rangesVillains = new ArrayList<>();
    }

    public void addRangeVillain(RangeIso rangeMoyenne) {
        RangeReelle rangeVillain = new RangeReelle(rangeMoyenne);
        this.rangesVillains.add(rangeVillain);
    }

    public RangeDenombrable getRangeHero() {
        return rangeHero;
    }

    public List<RangeReelle> getRangesVillains() {
        return rangesVillains;
    }
}
