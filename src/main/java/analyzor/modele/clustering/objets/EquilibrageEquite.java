package analyzor.modele.clustering.objets;

import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

public class EquilibrageEquite extends ObjetClusterisable {
    private final NoeudEquilibrage noeudEquilibrage;
    public EquilibrageEquite(NoeudEquilibrage noeudEquilibrage) {
        super();
        this.noeudEquilibrage = noeudEquilibrage;
    }

    @Override
    protected float[] valeursClusterisables() {
        return noeudEquilibrage.getEquiteFuture().aPlat();
    }

    public NoeudEquilibrage getNoeud() {
        return noeudEquilibrage;
    }
}
