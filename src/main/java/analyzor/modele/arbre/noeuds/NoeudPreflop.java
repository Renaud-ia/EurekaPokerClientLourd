package analyzor.modele.arbre.noeuds;

import jakarta.persistence.Entity;

@Entity
public class NoeudPreflop extends NoeudAction {
    public NoeudPreflop(Long idNoeudTheorique, float stackEffectif, float pot, float potBounty) {
        super(idNoeudTheorique, stackEffectif, pot, potBounty);
    }
}
