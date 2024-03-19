package analyzor.modele.clustering.objets;

import analyzor.modele.parties.Entree;

public class EntreeBetSize extends ObjetClusterisable {

    private final Entree entree;
    public EntreeBetSize(Entree entree) {
        this.entree = entree;
    }
    @Override
    public float[] valeursClusterisables() {
        float[] valeurs = new float[1];
        valeurs[0] = entree.getBetSize();
        return valeurs;
    }

    public Entree getEntree() {
        return entree;
    }
}
