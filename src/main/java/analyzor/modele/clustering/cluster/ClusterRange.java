package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ClusterRange extends ClusterDeBase<ComboPreClustering> {
    private ComboPreClustering centreGravite;
    public ClusterRange() {
        super();
    }
    public ClusterRange(List<ComboPreClustering> combos) {
        super(combos);
    }

    
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

    
    @Override
    public float distance(ObjetClusterisable autreObjet) {
        
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

    public ComboPreClustering getCentreGravite() {
        return centreGravite;
    }

    


}
