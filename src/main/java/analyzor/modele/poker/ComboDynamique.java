package analyzor.modele.poker;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.SituationIso;
import jakarta.persistence.*;

@Entity
public class ComboDynamique {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
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
