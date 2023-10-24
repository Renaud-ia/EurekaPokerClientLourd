package analyzor.modele.poker;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.SituationIso;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class RangeSauvegardable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private ProfilJoueur profil;
    @ManyToOne
    private SituationIso situation;
    @ManyToOne
    private Action action;
    private int nObservations;
    private float probabiliteAction;
    private float probEstimeeAction;
}
