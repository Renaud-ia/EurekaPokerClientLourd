package analyzor.modele.arbre.noeuds;

// interface pour homogénéiser le calcul de distances entre Situation et Action
public interface NoeudMesurable {
    float getPot();

    float getPotBounty();

    long getCodeStackEffectif();

    long getIdFormatSolution();

    long getIdNoeudSituation();
}
