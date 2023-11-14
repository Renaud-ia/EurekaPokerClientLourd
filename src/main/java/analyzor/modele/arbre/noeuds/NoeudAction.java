package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Move;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class NoeudAction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private final FormatSolution formatSolution;
    // pas besoin de préciser le type d'action car compris dans le noeud théorique
    // valeur qui va être en doublon car on a fusionné SPRB et action mais pas grave
    @Column(nullable = false)
    private Long idNoeudTheorique;
    private float betSize;
    @Column(nullable = false)
    private final float stackEffectif;
    @Column(nullable = false)
    private final float pot;
    @Column(nullable = false)
    private final float potBounty;

    @Transient
    private final Move move;

    public NoeudAction(FormatSolution formatSolution, Long idNoeudTheorique,
                       float stackEffectif, float pot, float potBounty) {
        this.formatSolution = formatSolution;
        this.idNoeudTheorique = idNoeudTheorique;
        this.stackEffectif = stackEffectif;
        this.pot = pot;
        this.potBounty = potBounty;

        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(idNoeudTheorique);
        this.move = noeudAbstrait.getMove();
    }

    public void setBetSize(float betSize) {
        this.betSize = betSize;
    }

}
