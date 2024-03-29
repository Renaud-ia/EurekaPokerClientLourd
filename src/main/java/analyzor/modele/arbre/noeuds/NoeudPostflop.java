package analyzor.modele.arbre.noeuds;


public class NoeudPostflop extends NoeudAction {
    
    public NoeudPostflop() {super();}
    private int idCluster;

    public NoeudPostflop(NoeudSituation noeudSituation, long idNoeudTheorique) {
        super(noeudSituation, idNoeudTheorique);
    }
}
