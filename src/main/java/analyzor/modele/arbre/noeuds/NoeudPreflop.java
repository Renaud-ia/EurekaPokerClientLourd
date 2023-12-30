package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;
import jakarta.persistence.Entity;

@Entity
public class NoeudPreflop extends NoeudAction {
    // pour hibernate
    public NoeudPreflop() {super();}
    public NoeudPreflop(Long idNoeudTheorique, float stackEffectif, float pot, float potBounty) {
        super(idNoeudTheorique, stackEffectif, pot, potBounty);
    }
}
