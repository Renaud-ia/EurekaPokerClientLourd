package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * type particulier de clustering
 * quand on va calculer le centroide, la distance avec un autre cluster, et l'homogénéité
 * se base sur l'équité future et pas sur la méthode classique (= valeursNormalisees)
 */
public class ClusterRange extends ClusterDeBase<ComboPreClustering> {
    private ComboPreClustering centreGravite;
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

        if (centroide == null) calculerCentroide();

        float somme = 0f;
        for (int i = 0; i < centroide.length; i++) {
            somme += (float) Math.pow(centroide[i] - ((ComboPreClustering) autreObjet).getEquiteFuture().aPlat()[i], 2);
        }

        return (float) Math.sqrt(somme);
    }

    @Override
    protected float[] valeursClusterisables() {
        // todo => à mettre dans getCentoide de BaseCluster (voir si ça clashe pas avec d'autres usages)
        if (centroide == null) calculerCentroide();

        return centroide;
    }

    @Override
    public String toString() {
        if (centreGravite == null) return "CLUSTER VIDE";
        return "CLUSTER AVEC CENTRE : [" + centreGravite.getNoeudEquilibrage() + "], homogénéité : " + homogeneite();
    }

    private NoeudEquilibrage comboCentral() {
        if (listeObjets == null || listeObjets.isEmpty()) return null;

        float min_distance = Float.MAX_VALUE;
        NoeudEquilibrage noeudCentral = null;

        for (ComboPreClustering combo : listeObjets) {
            float distance = this.distance(combo);
            if (distance < min_distance) {
                min_distance = distance;
                noeudCentral = combo.getNoeudEquilibrage();
            }
        }

        return noeudCentral;
    }

    public void setCentreGravite(ComboPreClustering centreGravite) {
        this.centreGravite = centreGravite;
    }

    /*
    /**
     * méthode custom de l'homogénéité, on ne prend qu'un % plus proche des combos
     * permet de compenser l'absence de valeurs aberrantes
     * @return la moyenne des distances au centroide

    public float homogeneite() {
        // todo on pourrait se baser sur pCombo plutôt que effectif ?
        float pctPrisEnCompte = 0.5f;

        List<ComboPreClustering> combosPrisEnCompte = new ArrayList<>();
        int compte = 0;

        float totalDistance = 0;
        while (compte < (getEffectif() * pctPrisEnCompte)) {
            float minDistance = Float.MAX_VALUE;
            ComboPreClustering comboPlusProche = null;
            for (ComboPreClustering membreCluster : getObjets()) {
                if (combosPrisEnCompte.contains(membreCluster)) continue;
                float distance = this.distance(membreCluster);

                if (distance < minDistance) {
                    minDistance = distance;
                    comboPlusProche = membreCluster;
                }
            }

            totalDistance += minDistance;
            compte++;
            combosPrisEnCompte.add(comboPlusProche);
        }

        return totalDistance / compte;
    }
    */


}
