package analyzor.modele.arbre.noeuds;

import jakarta.persistence.Entity;

@Entity
public class NoeudPreflop extends NoeudAction {
    // pour hibernate
    public NoeudPreflop() {super();}
    public NoeudPreflop(NoeudSituation noeudSituation, long idNoeudTheorique) {
        super(noeudSituation, idNoeudTheorique);
    }
}
