package analyzor.modele.poker;

import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.parties.ProfilJoueur;
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
    private NoeudPreflop noeudArbre;
    private int nObservations;
    private float probabiliteAction;
    private float probEstimeeAction;
}
