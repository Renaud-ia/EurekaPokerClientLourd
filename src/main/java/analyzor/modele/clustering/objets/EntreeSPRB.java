package analyzor.modele.clustering.objets;

import analyzor.modele.config.ValeursConfig;
import analyzor.modele.parties.Entree;

public class EntreeSPRB extends ObjetClusterisable {
    private final Entree entree;
    // poids des deux valeurs

    public EntreeSPRB(Entree entree) {
        this.entree = entree;
        setPoids(ValeursConfig.poidsSPRB);
    }
    @Override
    public float[] valeursClusterisables() {
        float[] valeurs = new float[3];
        valeurs[0] = entree.getStackEffectif();
        valeurs[1] = entree.getPotTotal();
        valeurs[2] = entree.getPotBounty();
        return valeurs;
    }

    /**
     * réécriture pour intégrer les poids
     * @param autreObjet autre objet de même classe
     * @return distance euclidienne pondérée
     */
    @Override
    public float distance(ObjetClusterisable autreObjet) {
        float[] p = valeursNormalisees();
        float[] q = autreObjet.valeursNormalisees();

        if (p.length != q.length || p.length != getPoids().length) {
            throw new IllegalArgumentException("Les deux tableaux doivent avoir la même taille.");
        }

        float somme = 0.0f;
        for (int i = 0; i < p.length; i++) {
            somme += (p[i] - q[i]) * (p[i] - q[i]) * getPoids()[i];
        }

        return (float) Math.sqrt(somme);
    }

    public Entree getEntree() {
        return entree;
    }
}
