package analyzor.modele.clustering.objets;

import analyzor.modele.parties.Entree;

public class EntreeSPRB extends ObjetClusterisable {
    protected final static float[] poidsSPRB = {1f, 1f, 1f};
    private final Entree entree;
    // poids des deux valeurs
    public EntreeSPRB(Entree entree) {
        this.entree = entree;
        setPoids(poidsSPRB);
    }
    @Override
    public float[] valeursClusterisables() {
        float[] valeurs = new float[3];
        valeurs[0] = entree.getStackEffectif();
        valeurs[1] = entree.getPotTotal();
        valeurs[2] = entree.getPotBounty();
        return valeurs;
    }

    public Entree getEntree() {
        return entree;
    }
}
