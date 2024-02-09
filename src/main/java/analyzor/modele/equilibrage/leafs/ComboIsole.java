package analyzor.modele.equilibrage.leafs;

import analyzor.modele.denombrement.combos.ComboDenombrable;

/**
 * combo qui ne fait pas partie d'un cluster
 * indépendant sur les changements de probabilités
 */
public class ComboIsole extends NoeudEquilibrage {
    final ComboDenombrable combo;
    private float[] probaFoldEquite;
    public ComboIsole(ComboDenombrable comboDenombrable) {
        super(comboDenombrable.getPCombo(),
                comboDenombrable.getObservations(),
                comboDenombrable.getShowdowns(),
                comboDenombrable.getEquiteFuture());

        this.combo = comboDenombrable;
    }

    public ComboDenombrable getComboDenombrable() {
        return combo;
    }



    public void fixerStrategie() {
        this.combo.setStrategie(this.getStrategieActuelle());
    }

    public void setProbabiliteFoldEquite(float[] proba) {
        this.probaFoldEquite = proba;
    }

    @Override
    public void initialiserStrategie(int pas) {
        if (probaObservations == null || probaFoldEquite == null)
            throw new RuntimeException("Les probabilités n'ont pas été correctement initialisées");

        int indexFold = probaObservations.length - 1;
        // on va seulement rédéfinir proba Fold
        float[] probaFoldObs = probaObservations[indexFold];
        float[] probaFoldFinale = new float[probaFoldObs.length];
        if (probaFoldObs.length != probaFoldEquite.length)
            throw new RuntimeException("Proba fold observations et équité n'ont pas la même taille");

        for (int i = 0; i < probaFoldObs.length; i++) {
            probaFoldFinale[i] = probaFoldObs[i] * probaFoldEquite[i];
        }

        normaliserProbabilites(probaFoldEquite);
        probaObservations[indexFold] = probaFoldEquite;

        strategieActuelle = new Strategie(probaObservations, pas);
    }


    @Override
    public String toString() {
        return "[COMBO ISOLE : " + combo + "]";
    }

    @Override
    public int hashCode() {
        return combo.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ComboIsole)) return false;
        return this.combo.equals( ((ComboIsole) o).combo);
    }
}
