package analyzor.modele.clustering.objets;

import analyzor.modele.equilibrage.leafs.ComboDansCluster;
import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import analyzor.modele.poker.evaluation.EquiteFuture;


public class ComboPreClustering extends ObjetClusterisable {

    private final ComboIsole noeudEquilibrage;
    private float[] valeursClustering;
    public ComboPreClustering(NoeudEquilibrage noeudEquilibrage) {

        if (!(noeudEquilibrage instanceof ComboIsole))
            throw new IllegalArgumentException("Le noeud doit être un combo isolé");

        this.noeudEquilibrage = (ComboIsole) noeudEquilibrage;
        fixerValeurs();
    }


    private void fixerValeurs() {
        float[] probaObservations = noeudEquilibrage.getProbabilites();
        float[] equiteAPlat = noeudEquilibrage.getEquiteFuture().aPlat();

        int tailleTotale = probaObservations.length + equiteAPlat.length;

        float[] valeursClusterisables = new float[tailleTotale];
        System.arraycopy(probaObservations, 0, valeursClusterisables, 0, probaObservations.length);
        System.arraycopy(equiteAPlat, 0, valeursClusterisables, probaObservations.length, equiteAPlat.length);

        this.valeursClustering = valeursClusterisables;
    }


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

    public ComboIsole getNoeudEquilibrage() {
        return noeudEquilibrage;
    }

    public EquiteFuture getEquiteFuture() {
        return noeudEquilibrage.getEquiteFuture();
    }

    public float getPCombo() {
        return noeudEquilibrage.getPCombo();
    }

    @Override
    public String toString() {
        return noeudEquilibrage.toString();
    }
}
