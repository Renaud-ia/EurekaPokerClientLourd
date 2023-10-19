package analyzor.modele.poker;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.SituationIso;
import jakarta.persistence.Entity;

@Entity
public class RangeSauvegardable {
    private ProfilJoueur profil;
    private SituationIso situation;
    private Action action;
    private int nObservations;
    private float probabiliteAction;
    private float probEstimeeAction;
}
