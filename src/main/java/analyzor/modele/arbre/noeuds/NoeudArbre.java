package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Action;
import analyzor.modele.parties.Situation;
import jakarta.persistence.*;

// tagger interface
@Entity
public class NoeudArbre {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private FormatSolution formatSolution;

    // ça va être utile pour simulation
    @ManyToOne
    public Situation situation;
    private Action action;
    private int minSPR;
    private int maxSPR;
    private int minPotBounty;
    private int maxPotBounty;

    @ManyToMany
    private Set<Entree> entrees;


    public Long getId() {
        return id;
    }
}
