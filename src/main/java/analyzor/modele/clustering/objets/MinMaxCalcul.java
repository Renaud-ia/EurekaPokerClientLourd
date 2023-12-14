package analyzor.modele.clustering.objets;

import analyzor.modele.equilibrage.NoeudEquilibrage;

import java.util.Arrays;
import java.util.List;

public class MinMaxCalcul<T extends ObjetClusterisable> {
    private float[] minValeurs;
    private float[] maxValeurs;

    public void calculerMinMax(List<T> objets) {
        this.calculerMinMax(Float.MAX_VALUE, Float.MIN_VALUE, objets);
    }

    // détermine les valeurs minimales et maximales
    //on peut imposer une valeur minimale et maximale évite les bugs ultérieurs quand valeur min = valeur max
    public void calculerMinMax(float valeurMin, float valeurMax, List<T> valeurs) {
        minValeurs = new float[valeurs.get(0).valeursClusterisables().length];
        Arrays.fill(minValeurs, valeurMin);
        maxValeurs = new float[valeurs.get(0).valeursClusterisables().length];
        Arrays.fill(maxValeurs, valeurMax);

        for (int i = 0; i < minValeurs.length; i++) {
            float minValeur = valeurMin;
            float maxValeur = valeurMax;

            for (T objet : valeurs) {
                float valeurNoeud = objet.valeursClusterisables()[i];
                if (valeurNoeud < minValeur) {
                    minValeur = valeurNoeud;
                }
                if (valeurNoeud > maxValeur) {
                    maxValeur = valeurNoeud;
                }
            }
            minValeurs[i] = minValeur;
            maxValeurs[i] = maxValeur;
        }

    }

    public float[] getMinValeurs() {
        return minValeurs;
    }

    public float[] getMaxValeurs() {
        return maxValeurs;
    }
}
