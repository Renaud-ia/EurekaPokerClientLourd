package analyzor.modele.poker;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.SituationIso;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class ComboDynamique {
    private int equiteFlop;
    private int equiteTurn;
    private int equiteRiver;

    @ManyToOne
    private SituationIso situation;

    @ManyToOne
    private Action action;

    // constructeurs
    public ComboDynamique() {}
    public ComboDynamique(HistogrammeEquite histogrammeEquite) {

    }
}
