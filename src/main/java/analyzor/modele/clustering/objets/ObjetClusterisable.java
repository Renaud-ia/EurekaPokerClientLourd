package analyzor.modele.clustering.objets;

import java.util.Arrays;

public abstract class ObjetClusterisable {
    protected float[] valeursMin;
    protected float[] valeursMax;
    protected boolean minMaxNormalisation = false;
    protected boolean logNormalisation = false;
    // si poids = null => points équivalents
    protected float[] poids;
    protected abstract float[] valeursClusterisables();

    public float distance(ObjetClusterisable autreObjet) {
        float[] poidsUtilise = getPoids();

        float[] valeursObjet = valeursNormalisees();
        float[] valeursAutreObjet = autreObjet.valeursNormalisees();

        if (valeursObjet.length != valeursAutreObjet.length) {
            throw new IllegalArgumentException("Les deux tableaux doivent avoir la même taille.");
        }

        float sommePoids = 0f;
        float somme = 0.0f;
        for (int i = 0; i < valeursObjet.length; i++) {
            somme += (valeursObjet[i] - valeursAutreObjet[i]) * (valeursObjet[i] - valeursAutreObjet[i]) * poidsUtilise[i];
            sommePoids += poidsUtilise[i];
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
        if (minValeurs.length != maxValeurs.length)
            throw new IllegalArgumentException("Pas la même dimension de valeurs max et min");

        for (int i = 0; i < minValeurs.length; i++) {
            if (minValeurs[i] == maxValeurs[i]) throw new IllegalArgumentException("Même valeur min et max");
        }

        this.valeursMin = minValeurs;
        this.valeursMax = maxValeurs;
        this.minMaxNormalisation = true;
    }

    /**
     * applique la normalisation selon les critères fixés préalablement
     * ne modifie pas les données initiales
     * interface pour récupérer les données
     */
    public float[] valeursNormalisees() {
        float[] valeursNormalisees = new float[valeursClusterisables().length];

        for (int indexValeur = 0; indexValeur < valeursClusterisables().length; indexValeur++) {
            float valeurNormalisee = valeursClusterisables()[indexValeur];

            if (logNormalisation) {
                valeurNormalisee = (float) Math.log(valeurNormalisee);
            }

            if (minMaxNormalisation) {
                valeurNormalisee =
                        (valeurNormalisee - valeursMin[indexValeur]) / (valeursMax[indexValeur] - valeursMin[indexValeur]);
            }

            valeursNormalisees[indexValeur] = valeurNormalisee;
        }

        return valeursNormalisees;
    }

    /**
     * si le poids n'a pas été fixé, on fixe à 1 chaque dimension => poids équivalent
     */
    public float[] getPoids() {
        if (poids == null) {
            poids = new float[this.nDimensions()];
            Arrays.fill(poids, 1);
        }

        return poids;
    }

}
