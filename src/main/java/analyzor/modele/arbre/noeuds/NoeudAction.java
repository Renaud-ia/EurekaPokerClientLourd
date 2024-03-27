package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.RangeSauvegardable;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class NoeudAction implements NoeudMesurable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long idNoeudTheorique;

    @ManyToOne
    @JoinColumn(nullable = false)
    private NoeudSituation noeudSituation;

    private float betSize;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(nullable = true)
    private RangeSauvegardable range;

    // pour hibernate
    public NoeudAction() {}

    public NoeudAction(NoeudSituation noeudSituation, long idNoeudTheorique) {
        this.idNoeudTheorique = idNoeudTheorique;
        this.noeudSituation = noeudSituation;
    }

    public void setBetSize(float betSize) {
        this.betSize = betSize;
    }

    @Override
    public long getCodeStackEffectif() {
        return noeudSituation.getCodeStackEffectif();
    }

    @Override
    public float getPot() {
        return noeudSituation.getPot();
    }

    @Override
    public float getPotBounty() {
        return noeudSituation.getPotBounty();
    }

    public float getBetSize() {
        return betSize;
    }

    public void setRange (RangeSauvegardable range) {
        this.range = range;
    }

    public RangeSauvegardable getRange() {
        return range;
    }

    public Move getMove() {
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(idNoeudTheorique);
        return noeudAbstrait.getMove();
    }

    public long getIdNoeud() {
        return idNoeudTheorique;
    }

    @Override
    public long getIdFormatSolution() {
        return noeudSituation.getIdFormatSolution();
    }

    @Override
    public long getIdNoeudSituation() {
        return noeudSituation.getIdNoeudSituation();
    }
}
