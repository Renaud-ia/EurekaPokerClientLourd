package analyzor.modele.poker.evaluation;

import java.util.HashMap;

public class ConfigCalculatrice {
    // todo rajouter la taille de subset voulue dans la config
    // todo voir si avec les modifs on peut mettre pctRangeHero = 1 maximum
    /**
     * stocke les valeurs de configuration selon usage calculatrice
     */
    // range utilisée selon nombre de cartes ajoutées
    protected HashMap<Integer, Float> pctRangeHero;
    protected HashMap<Integer, Float> pctRangeVillain;
    protected int nombreSubsets;
    protected HashMap<Integer, Integer> nSimus;
    protected int nPercentiles;

    public ConfigCalculatrice() {
        nPercentiles = 5;
    };

    public void modePrecision() {
        pctRangeHero = new HashMap<>();
        pctRangeHero.put(0, 3f);
        pctRangeHero.put(3, 1f);
        pctRangeHero.put(4, 0.5f);
        pctRangeHero.put(5, 0.3f);

        pctRangeVillain = new HashMap<>();
        pctRangeVillain.put(0, 3f);
        pctRangeVillain.put(3, 1f);
        pctRangeVillain.put(4, 0.5f);
        pctRangeVillain.put(5, 0.3f);

        nombreSubsets = 111;

        nSimus = new HashMap<>();
        nSimus.put(2, 100);
        nSimus.put(3, 200);
        nSimus.put(4, 300);
        nSimus.put(5, 500);
    }

    public void modeRapide() {
        pctRangeHero = new HashMap<>();
        pctRangeHero.put(0, 0.05f);
        pctRangeHero.put(3, 0.05f);
        pctRangeHero.put(4, 0.05f);
        pctRangeHero.put(5, 0.05f);

        pctRangeVillain = new HashMap<>();
        pctRangeVillain.put(0, 0.05f);
        pctRangeVillain.put(3, 0.05f);
        pctRangeVillain.put(4, 0.05f);
        pctRangeVillain.put(5, 0.05f);

        nombreSubsets = 44;

        nSimus = new HashMap<>();
        nSimus.put(2, 100);
        nSimus.put(3, 200);
        nSimus.put(4, 300);
        nSimus.put(5, 500);
    }

    public void modeExact() {
        pctRangeHero = new HashMap<>();
        pctRangeHero.put(0, 3f);
        pctRangeHero.put(3, 1f);
        pctRangeHero.put(4, 0.5f);
        pctRangeHero.put(5, 0.3f);

        pctRangeVillain = new HashMap<>();
        pctRangeVillain.put(0, 3f);
        pctRangeVillain.put(3, 1f);
        pctRangeVillain.put(4, 0.5f);
        pctRangeVillain.put(5, 0.3f);

        nombreSubsets = 1755;

        // todo trouver ds valeurs qui prennent pas des plombes non plus
        nSimus = new HashMap<>();
        nSimus.put(2, 500);
        nSimus.put(3, 1755);
        nSimus.put(4, 10000);
        nSimus.put(5, 20000);
    }
}
