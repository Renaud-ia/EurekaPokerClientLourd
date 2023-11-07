package analyzor.modele.clustering.objets;

public abstract class ObjetClusterisable {
    public abstract float[] valeursClusterisables();

    public float distance(ObjetClusterisable autreObjet) {
        float[] p = valeursClusterisables();
        float[] q = autreObjet.valeursClusterisables();

        if (p.length != q.length) {
            throw new IllegalArgumentException("Les deux tableaux doivent avoir la même taille.");
        }

        float somme = 0.0f;
        for (int i = 0; i < p.length; i++) {
            somme += (p[i] - q[i]) * (p[i] - q[i]);
        }

        return (float) Math.sqrt(somme);
    }

    public int nDimensions() {
        return valeursClusterisables().length;
    }
}
