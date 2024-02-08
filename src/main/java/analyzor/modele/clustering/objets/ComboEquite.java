package analyzor.modele.clustering.objets;

import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

/**
 * représentation d'un noeud
 */
public class ComboEquite extends ObjetClusterisable {
    private final NoeudEquilibrage noeudEquilibrage;
    public ComboEquite(NoeudEquilibrage noeudEquilibrage) {
        this.noeudEquilibrage = noeudEquilibrage;
    }
    @Override
    protected float[] valeursClusterisables() {
        return noeudEquilibrage.getEquiteFuture().valeursNormalisees();
    }

    public float distanceProbabilites(ComboEquite comboEquite) {
        if (this.noeudEquilibrage.getProbabilites().length != comboEquite.noeudEquilibrage.getProbabilites().length)
            throw new IllegalArgumentException("Pas le même nombre de probabilités dans les objets comparés");

        float distanceCarree = 0;
        for (int i = 0; i < noeudEquilibrage.getProbabilites().length; i++) {
            distanceCarree += (float) Math.pow(
                    this.noeudEquilibrage.getProbabilites()[i] -
                            comboEquite.noeudEquilibrage.getProbabilites()[i], 2);
        }

        return (float) Math.sqrt(distanceCarree);
    }

    public NoeudEquilibrage getNoeudEquilibrage() {
        return noeudEquilibrage;
    }
}
