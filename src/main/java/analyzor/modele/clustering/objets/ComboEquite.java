package analyzor.modele.clustering.objets;

import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

/**
 * représentation d'un noeud
 */
public class ComboEquite extends ObjetClusterisable {
    // todo trouver les bonnes valeurs
    private static final float POIDS_EQUITE = 1;
    private static final float POIDS_PROBABILITES = 1;
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

    public float distanceEquite(ComboEquite pointIsole) {
        if (this.noeudEquilibrage.getEquiteFuture().valeursNormalisees().length
                != pointIsole.noeudEquilibrage.getEquiteFuture().valeursNormalisees().length)
            throw new IllegalArgumentException("Pas la même taille d'équité");

        float distanceCarree = 0;
        for (int i = 0; i < noeudEquilibrage.getEquiteFuture().valeursNormalisees().length; i++) {
            distanceCarree += (float) Math.pow(
                    this.noeudEquilibrage.getEquiteFuture().valeursNormalisees()[i] -
                            pointIsole.noeudEquilibrage.getEquiteFuture().valeursNormalisees()[i], 2);
        }

        return (float) Math.sqrt(distanceCarree);
    }

    public float distancePonderee(ComboEquite comboEquite) {
        return (distanceEquite(comboEquite) * POIDS_EQUITE + distanceProbabilites(comboEquite) * POIDS_PROBABILITES);
    }

    public NoeudEquilibrage getNoeudEquilibrage() {
        return noeudEquilibrage;
    }


}
