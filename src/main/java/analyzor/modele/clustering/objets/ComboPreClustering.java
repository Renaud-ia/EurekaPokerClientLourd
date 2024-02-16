package analyzor.modele.clustering.objets;

import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

/**
 * utilisé pour le clustering des centres de gravité lors du clustering de range
 * permet de fixer manuellement la distance après avoir transformé les données par une ACP
 */
public class ComboPreClustering extends ObjetClusterisable {
    // todo refactoriser avec ComboPostClustering
    private final NoeudEquilibrage noeudEquilibrage;
    private float[] valeursClustering;
    public ComboPreClustering(NoeudEquilibrage noeudEquilibrage) {
        this.noeudEquilibrage = noeudEquilibrage;

        fixerValeurs();
    }

    /**
     * valeurs originales pour construire l'ACP
     */
    private void fixerValeurs() {
        float[] probaObservations = noeudEquilibrage.getProbabilites();
        float[] equiteAPlat = noeudEquilibrage.getEquiteFuture().aPlat();

        int tailleTotale = probaObservations.length + equiteAPlat.length;

        float[] valeursClusterisables = new float[tailleTotale];
        System.arraycopy(probaObservations, 0, valeursClusterisables, 0, probaObservations.length);
        System.arraycopy(equiteAPlat, 0, valeursClusterisables, probaObservations.length, equiteAPlat.length);

        this.valeursClustering = valeursClusterisables;
    }

    /**
     * nouvelles valeurs fixées manuellement => sera utile pour le KMEANS
     * @param data nouvelles données
     */
    public void setDonneesClusterisables(double[] data) {
        this.valeursClustering = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            valeursClustering[i] = (float) data[i];
        }
    }

    @Override
    protected float[] valeursClusterisables() {
        return valeursClustering;
    }

    public NoeudEquilibrage getNoeudEquilibrage() {
        return noeudEquilibrage;
    }
}
