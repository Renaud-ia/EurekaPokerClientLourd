package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.RangeDenombrable;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.poker.RangeReelle;

import java.util.ArrayList;
import java.util.List;

public class OppositionRange {
    private RangeDenombrable rangeHero;
    
    private List<RangeReelle> rangesVillains;

    public void setRangeHero(RangeIso rangeHero) {
        this.rangeHero = rangeHero;
        this.rangesVillains = new ArrayList<>();
    }

    @Deprecated
    public void addRangeVillain(RangeIso rangeMoyenne) {
        RangeReelle rangeVillain = new RangeReelle(rangeMoyenne);
        this.rangesVillains.add(rangeVillain);
    }

    public RangeDenombrable getRangeHero() {
        return rangeHero;
    }

    @Deprecated
    public List<RangeReelle> getRangesVillains() {
        return rangesVillains;
    }
}
