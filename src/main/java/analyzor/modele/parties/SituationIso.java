package analyzor.modele.parties;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SITUATION_ISO")
public class SituationIso extends Situation {
    private int code;
    public SituationIso() {};
    public SituationIso(int rang, int nJoueursActifs, int tour, int position, int code) {
        super(rang, nJoueursActifs, tour, position);
        this.code = code;
    }
}
