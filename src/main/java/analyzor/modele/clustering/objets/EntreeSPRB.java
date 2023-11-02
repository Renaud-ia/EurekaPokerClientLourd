package analyzor.modele.clustering.objets;

import analyzor.modele.config.ValeursConfig;
import analyzor.modele.parties.Entree;

public class EntreeSPRB extends ObjetClusterisable {
    private final Entree entree;
    protected float[] poids;
    // poids des deux valeurs

    public EntreeSPRB(Entree entree) {
        this.entree = entree;
        poids = ValeursConfig.poidsSPRB;
    }
    @Override
    public float[] valeursClusterisables() {
        float[] valeurs = new float[2];
        valeurs[0] = entree.getStackEffectif() / (entree.getPotTotal());
        valeurs[1] = entree.getPotBounty();
        return valeurs;
    }

    /**
     * réécriture pour intégrer les poids
     * @param autreObjet autre objet de même classe
     * @return distance euclidienne pondérée
     */
    @Override
    public float distance(ObjetClusterisable autreObjet) {
        float[] p = valeursClusterisables();
        float[] q = autreObjet.valeursClusterisables();

        if (p.length != q.length || p.length != poids.length) {
            throw new IllegalArgumentException("Les deux tableaux doivent avoir la même taille.");
        }

        float somme = 0.0f;
        for (int i = 0; i < p.length; i++) {
            somme += (p[i] - q[i]) * (p[i] - q[i]) * poids[i];
        }

        return (float) Math.sqrt(somme);
    }

    public Entree getEntree() {
        return entree;
    }
}
