package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.List;

/**
 * type particulier de clustering
 * quand on va calculer le centroide, la distance avec un autre cluster, et l'homogénéité
 * se base sur l'équité future et pas sur la méthode classique (= valeursNormalisees)
 */
public class ClusterRange extends ClusterDeBase<ComboPreClustering> {
    public ClusterRange() {
        super();
    }
    public ClusterRange(List<ComboPreClustering> combos) {
        super(combos);
    }

    /**
     * le centroide est calculé sur la base de l'équité future
     */
    @Override
    public void calculerCentroide() {
        if (listeObjets == null || listeObjets.isEmpty()) return;

        int nombrePoints = this.listeObjets.getFirst().getEquiteFuture().nDimensions();
        int nombreElements = this.listeObjets.size();
        float[] centroide = new float[nombrePoints];

        for (ComboPreClustering combo : this.listeObjets) {
            for (int j = 0; j < nombrePoints; j++) {
                centroide[j] += combo.getEquiteFuture().aPlat()[j] / nombreElements;
            }
        }
        this.centroide = centroide;
    }

    /**
     * la distance est calculée sur l'équite future
     */
    @Override
    public float distance(ObjetClusterisable autreObjet) {
        // distance des centroides donc pas de souci
        if (autreObjet instanceof ClusterRange) return super.distance(autreObjet);

        if (!(autreObjet instanceof ComboPreClustering))
            throw new IllegalArgumentException("Impossible de calculer la distance " +
                    "pour autre chose qu'un cluster et/ou combo");

        float somme = 0f;
        for (int i = 0; i < centroide.length; i++) {
            somme += (float) Math.pow(centroide[i] - ((ComboPreClustering) autreObjet).getEquiteFuture().aPlat()[i], 2);
        }

        return (float) Math.sqrt(somme);
    }


}
