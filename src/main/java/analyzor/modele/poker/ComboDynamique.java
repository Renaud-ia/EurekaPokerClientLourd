package analyzor.modele.poker;

import analyzor.modele.parties.TourMain;
import analyzor.modele.poker.evaluation.EquiteFuture;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ComboDynamique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long equiteFlop;
    private long equiteTurn;
    private long equiteRiver;


    public ComboDynamique() {}

    public ComboDynamique(EquiteFuture equiteFuture) {

        equiteFlop = equiteFuture.getEquite(TourMain.Round.FLOP);
        equiteTurn = equiteFuture.getEquite(TourMain.Round.TURN);
        equiteRiver = equiteFuture.getEquite(TourMain.Round.RIVER);
    }
}
