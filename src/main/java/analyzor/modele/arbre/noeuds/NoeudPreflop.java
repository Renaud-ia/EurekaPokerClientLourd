package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;
import jakarta.persistence.Entity;

@Entity
public class NoeudPreflop extends NoeudAction {
    public NoeudPreflop(FormatSolution formatSolution, Long idNoeudTheorique, float stackEffectif, float pot, float potBounty) {
        super(formatSolution, idNoeudTheorique, stackEffectif, pot, potBounty);
    }
}
