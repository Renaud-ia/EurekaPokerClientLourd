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
    @Transient
    public static int currentIndex = 0;
    @Transient
    private int indexHashMap;

    // pour hibernate
    public NoeudAction() {};

    public NoeudAction(Long idNoeudTheorique,
                       float stackEffectif, float pot, float potBounty) {
        this.idNoeudTheorique = idNoeudTheorique;
        this.stackEffectif = stackEffectif;
        this.pot = pot;
        this.potBounty = potBounty;

        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(idNoeudTheorique);
        this.move = noeudAbstrait.getMove();
        this.indexHashMap = currentIndex++;
    }

    public void setBetSize(float betSize) {
        this.betSize = betSize;
    }

    public float getStackEffectif() {
        return stackEffectif;
    }

    public float getPot() {
        return pot;
    }

    public float getPotBounty() {
        return potBounty;
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

    // attention peut buguer en cas de multithreading
    // todo : DEGUEULASSE remplacer par les attributs ou arrêter les HashMap ??
    @Override
    public int hashCode() {
        return this.indexHashMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoeudAction)) return false;
        return (((NoeudAction) o).indexHashMap == this.indexHashMap);
    }

    public void setFormatSolution(FormatSolution formatSolution) {
        this.formatSolution = formatSolution;
    }
}
