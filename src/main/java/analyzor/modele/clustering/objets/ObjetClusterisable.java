package analyzor.modele.clustering.objets;

import java.util.Arrays;

/**
 * classe de base pour créer des objets clusterisables
 * très flexible peut intégrer
 */
public abstract class ObjetClusterisable {
    private float[] valeursMin;
    private float[] valeursMax;
    private float[] valeursNormalisees;
    private boolean minMaxNormalisation = false;
    private boolean logNormalisation = false;
    // si poids = null => points équivalents
    private float[] poids;
    protected abstract float[] valeursClusterisables();

    protected float distanceCarree(ObjetClusterisable autreObjet) {
        float[] valeursAutreObjet = autreObjet.valeursNormalisees();

        return distanceCarree(valeursAutreObjet);
    }

    private float distanceCarree(float[] valeursAutreObjet) {
        float[] poidsUtilise = getPoids();
        float[] valeursObjet = valeursNormalisees();

        if (valeursObjet.length != valeursAutreObjet.length) {
            throw new IllegalArgumentException("Les deux tableaux doivent avoir la même taille.");
        }

        float sommePoids = 0f;
        float somme = 0.0f;
        for (int i = 0; i < valeursObjet.length; i++) {
            somme += (valeursObjet[i] - valeursAutreObjet[i]) * (valeursObjet[i] - valeursAutreObjet[i]) * poidsUtilise[i];
            sommePoids += poidsUtilise[i];
        }

        return somme;
    }

    public float distance(ObjetClusterisable autreObjet) {
        return (float) Math.sqrt(distanceCarree(autreObjet));
    }

    public float distance(float[] valeurs) {
        return (float) Math.sqrt(distanceCarree(valeurs));
    }

    public int nDimensions() {
        return valeursClusterisables().length;
    }

    public void activerLogNormalisation() {
        logNormalisation = true;
        valeursNormalisees = null;
    }

    public void activerMinMaxNormalisation(float[] minValeurs, float[] maxValeurs) {
        if (minValeurs.length != maxValeurs.length)
            throw new IllegalArgumentException("Pas la même dimension de valeurs max et min");

        if (minValeurs.length != valeursClusterisables().length)
            throw new IllegalArgumentException("Pas la même dimension de valeurs max et valeursClusterisables");

        this.valeursMin = minValeurs;
        this.valeursMax = maxValeurs;
        this.minMaxNormalisation = true;
        valeursNormalisees = null;
    }

    /**
     * applique la normalisation selon les critères fixés préalablement
     * ne modifie pas les données initiales
     * interface pour récupérer les données
     */
    public float[] valeursNormalisees() {
        // on garde en mémoire pour optimisation
        if (valeursNormalisees != null) return valeursNormalisees;

        float[] valeursClusterisables = valeursClusterisables();
        valeursNormalisees = new float[valeursClusterisables.length];

        for (int indexValeur = 0; indexValeur < valeursClusterisables.length; indexValeur++) {
            float valeurNormalisee = valeursClusterisables[indexValeur];

            if (logNormalisation) {
                valeurNormalisee = (float) Math.log(valeurNormalisee);
            }

            if (minMaxNormalisation) {
                // ce cas va parfois arriver (par ex potBounty quand pas de bounty)
                if (valeursMin[indexValeur] == valeursMax[indexValeur]) valeurNormalisee = 0;
                else valeurNormalisee =
                        (valeurNormalisee - valeursMin[indexValeur])
                                / (valeursMax[indexValeur] - valeursMin[indexValeur]);
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

    public void setPoids(float[] poids) {
        this.poids = poids;
    }

    public void normalisationActivee(boolean activee) {
        minMaxNormalisation = false;
        logNormalisation = false;
        valeursNormalisees = null;
    }
}
