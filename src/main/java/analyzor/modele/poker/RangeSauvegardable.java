package analyzor.modele.poker;

import analyzor.modele.arbre.noeuds.NoeudArbre;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Action;
import analyzor.modele.parties.ProfilJoueur;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class RangeSauvegardable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    FormatSolution formatSolution;
    @ManyToOne
    private ProfilJoueur profil;
    @ManyToOne
    private NoeudArbre noeudArbre;
    @ManyToOne
    private Action actionIso;
    private int nObservations;
    private float probabiliteAction;
    private float probEstimeeAction;
}
