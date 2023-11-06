package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class NoeudAction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private FormatSolution formatSolution;
    // pas besoin de préciser le type d'action car compris dans le noeud théorique
    private Long idNoeudTheorique;
    private float betSize;
    private float stackEffectif;
    private float pot;
    private float potBounty;
}
