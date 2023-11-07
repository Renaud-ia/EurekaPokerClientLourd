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
    private FormatSolution formatSolution;
    // pas besoin de préciser le type d'action car compris dans le noeud théorique
    // valeur qui va être en doublon car on a fusionné SPRB et action mais pas grave
    @Column(nullable = false)
    private Long idNoeudTheorique;
    private float betSize;
    @Column(nullable = false)
    private float stackEffectif;
    @Column(nullable = false)
    private float pot;
    @Column(nullable = false)
    private float potBounty;

    @Transient
    private Move move;

    public NoeudAction(Long idNoeudTheorique, float stackEffectif, float pot, float potBounty) {
        this.idNoeudTheorique = idNoeudTheorique;
        this.stackEffectif = stackEffectif;
        this.pot = pot;
        this.potBounty = potBounty;

        //todo : vérifier dans les tests que c'est ok!
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(idNoeudTheorique);
        this.move = noeudAbstrait.getMove();
    }

    public void setBetSize(float betSize) {
        this.betSize = betSize;
    }

    public void setFormatSolution(FormatSolution formatSolution) {
        this.formatSolution = formatSolution;
    }


    public Move getMove() {
        return move;
    }
}
