package analyzor.modele.parties;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("SITUATION_ISO")
public class SituationIso extends Situation {
    private int methode;
    private int idCluster;
    private int minSPR;
    private int maxSPR;
    private int minPotBounty;
    private int maxPotBounty;

    @OneToMany(mappedBy = "situation")
    private List<Entree> entrees = new ArrayList<>();

    public SituationIso() {};
    public SituationIso(int rang, int nJoueursActifs, TourMain.Round tour, int position) {
        super(rang, nJoueursActifs, tour, position);

    }
}
