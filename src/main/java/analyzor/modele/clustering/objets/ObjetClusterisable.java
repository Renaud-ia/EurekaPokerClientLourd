package analyzor.modele.clustering.objets;

public abstract class ObjetClusterisable {
    protected float[] valeursMin;
    protected float[] valeursMax;
    protected boolean minMaxNormalisation = false;
    protected boolean logNormalisation = false;
    public abstract float[] valeursClusterisables();

    public float distance(ObjetClusterisable autreObjet) {
        float[] p = valeursClusterisables();
        float[] q = autreObjet.valeursClusterisables();

        if (p.length != q.length) {
            throw new IllegalArgumentException("Les deux tableaux doivent avoir la même taille.");
        }

        float somme = 0.0f;
        for (int i = 0; i < p.length; i++) {
            float valeurObjet = getValeurNormalisee(p, i);
            float valeurAutreObjet = getValeurNormalisee(q, i);
            somme += (valeurObjet - valeurAutreObjet) * (valeurObjet - valeurAutreObjet);
        }

        return (float) Math.sqrt(somme);
    }

    public int nDimensions() {
        return valeursClusterisables().length;
    }

    public void activerLogNormalisation() {
        logNormalisation = true;
    }

    public void activerMinMaxNormalisation(float[] minValeurs, float[] maxValeurs) {
        this.valeursMin = minValeurs;
        this.valeursMax = maxValeurs;
        this.minMaxNormalisation = true;
    }

    /**
     * applique la normalisation selon les critères fixés préalablement
     * ne modifie pas les données initiales
     */
    private float getValeurNormalisee(float[] tableauValeurs, int indexValeur) {
        float valeurNormalisee = tableauValeurs[indexValeur];

        if (logNormalisation) {
            valeurNormalisee = (float) Math.log(valeurNormalisee);
        }
        if (minMaxNormalisation) {
            valeurNormalisee =
                    (valeurNormalisee - valeursMin[indexValeur]) / (valeursMax[indexValeur] - valeursMin[indexValeur]);
        }

        return valeurNormalisee;
    }

}
