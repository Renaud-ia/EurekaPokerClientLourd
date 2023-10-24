package analyzor.modele.poker.evaluation;

import java.util.HashMap;

public class ConfigCalculatrice {
    /**
     * stocke les valeurs de configuration selon usage calculatrice
     */
    // range utilisée selon nombre de cartes ajoutées
    protected HashMap<Integer, Float> pctRangeHero;
    protected HashMap<Integer, Float> pctRangeVillain;
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

        nSimus = new HashMap<>();
        nSimus.put(2, 100);
        nSimus.put(3, 200);
        nSimus.put(4, 300);
        nSimus.put(5, 500);
    }
}
