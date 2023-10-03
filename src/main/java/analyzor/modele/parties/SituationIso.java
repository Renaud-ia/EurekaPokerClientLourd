package analyzor.modele.parties;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SITUATION_ISO")
public class SituationIso extends Situation {
    private int methode;
    private int idCluster;
    private int minSPR;
    private int maxSPR;
    private int minPotBounty;
    private int maxPotBounty;
    public SituationIso() {};
    public SituationIso(int rang, int nJoueursActifs, int tour, int position) {
        super(rang, nJoueursActifs, tour, position);

    }
}
