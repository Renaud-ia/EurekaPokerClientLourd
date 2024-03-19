package analyzor.modele.clustering.objets;

import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

/**
 * représentation d'un combo isolé pour le clustering
 * implémente trois systèmes de mesure : par équité, par probabilité et hybride
 */
public class ComboPostClustering extends ObjetClusterisable {
    // todo trouver les bonnes valeurs
    private static final float POIDS_EQUITE = 1;
    private static final float POIDS_PROBABILITES = 1;
    private final NoeudEquilibrage noeudEquilibrage;
    public ComboPostClustering(NoeudEquilibrage noeudEquilibrage) {
        this.noeudEquilibrage = noeudEquilibrage;
    }
    @Override
    protected float[] valeursClusterisables() {
        return noeudEquilibrage.getEquiteFuture().valeursNormalisees();
    }

    public float distanceProbabilites(ComboPostClustering comboPostClustering) {
        if (this.noeudEquilibrage.getProbabilites().length != comboPostClustering.noeudEquilibrage.getProbabilites().length)
            throw new IllegalArgumentException("Pas le même nombre de probabilités dans les objets comparés");

        float distanceCarree = 0;
        for (int i = 0; i < noeudEquilibrage.getProbabilites().length; i++) {
            distanceCarree += (float) Math.pow(
                    this.noeudEquilibrage.getProbabilites()[i] -
                            comboPostClustering.noeudEquilibrage.getProbabilites()[i], 2);
        }

        return (float) Math.sqrt(distanceCarree);
    }

    public float distanceEquite(ComboPostClustering pointIsole) {
        // todo pour rapidité implémenter la matrice d'équité ici aussi
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

    public float distancePonderee(ComboPostClustering comboPostClustering) {
        return (distanceEquite(comboPostClustering) * POIDS_EQUITE + distanceProbabilites(comboPostClustering) * POIDS_PROBABILITES);
    }

    public NoeudEquilibrage getNoeudEquilibrage() {
        return noeudEquilibrage;
    }


}
