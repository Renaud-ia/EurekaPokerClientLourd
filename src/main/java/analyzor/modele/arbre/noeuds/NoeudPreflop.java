package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Action;
import jakarta.persistence.*;

@Entity
public class NoeudPreflop {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private FormatSolution formatSolution;
    private Long idNoeudTheorique;
    private Action action;
    private int minSPR;
    private int maxSPR;
    private int minPotBounty;
    private int maxPotBounty;
}
