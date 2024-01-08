package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.ProfilJoueur;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class NoeudAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long idNoeudTheorique;

    @ManyToOne(fetch = FetchType.EAGER)
    private NoeudSituation noeudSituation;

    private float betSize;

    @Transient
    private Move move;

    // pour hibernate
    public NoeudAction() {};

    public NoeudAction(NoeudSituation noeudSituation, long idNoeudTheorique) {
        this.noeudSituation = noeudSituation;
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(idNoeudTheorique);
        this.move = noeudAbstrait.getMove();
    }

    public void setBetSize(float betSize) {
        this.betSize = betSize;
    }

    public float getStackEffectif() {
        return noeudSituation.getStackEffectif();
    }

    public float getPot() {
        return noeudSituation.getPot();
    }

    public float getPotBounty() {
        return noeudSituation.getPotBounty();
    }

    public float getBetSize() {
        return betSize;
    }

    @Override
    public String toString() {
        if (move == null) {
            return "root";
        }
        else return move + " " + Math.round(betSize) + "bb";
    }

    public Move getMove() {
        return move;
    }
}
