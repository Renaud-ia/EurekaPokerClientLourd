package analyzor.modele.equilibrage.leafs;

import java.util.HashMap;


public class ComboDansCluster extends ComboIsole {
    private final static float POIDS_AUTRE_COMBOS = 10f;
    private final static float ACCENTUATION_DISTANCE = 2.5f;
    private final ClusterEquilibrage cluster;
    private final HashMap<ComboDansCluster, Float> tablePoids;
    private float sommeInfluence;

    protected ComboDansCluster(ComboIsole comboIsole, ClusterEquilibrage cluster) {
        super(comboIsole.getComboDenombrable());
        this.cluster = cluster;
        this.probasStrategie = comboIsole.probasStrategie;
        tablePoids = new HashMap<>();

        this.probaFoldEquite = comboIsole.getProbaFoldEquite();
        this.isNotFolded = comboIsole.statutNotFolded();
    }

    
    @Override
    protected float probabiliteChangement(int indexAction, int sensChangement) {
        if (tablePoids.isEmpty()) construireTablePoids();
        float probaPropre = strategieActuelle.probaInterne(indexAction, sensChangement);
        if (probaPropre == -1) return -1;

        int valeurAction = strategieActuelle.getValeur(indexAction);

        float sommeAutreProba = 0;
        for (ComboDansCluster autreComboCluster : tablePoids.keySet()) {
            if (autreComboCluster == this) continue;
            float influenceAutreCombo = tablePoids.get(autreComboCluster);
            float probaChangement = autreComboCluster.probaInterneValeurFixee(indexAction, valeurAction, sensChangement);

            if (probaChangement > 0) {
                sommeAutreProba += influenceAutreCombo * probaChangement;
            }
        }

        sommeAutreProba /= sommeInfluence;

        return (probaPropre + POIDS_AUTRE_COMBOS * sommeAutreProba) / (1 + POIDS_AUTRE_COMBOS);

    }

    
    private float probaInterneValeurFixee(int indexAction, int valeurActuelle, int sensChangement) {
        return strategieActuelle.probaInterne(indexAction, valeurActuelle, sensChangement);
    }

    private void construireTablePoids() {
        for (ComboDansCluster autreComboCluster : cluster.getCombos()) {
            if (autreComboCluster == this) continue;
            float influenceAutreCombo = transformationDistance(autreComboCluster.getEquiteFuture().distance(this.equiteFuture));
            tablePoids.put(autreComboCluster, influenceAutreCombo);
            sommeInfluence += influenceAutreCombo;
        }
    }

    
    private float transformationDistance(float distance) {
        return (float) (1 / Math.pow(distance, ACCENTUATION_DISTANCE));
    }

    
    @Override
    public String toString() {
        return "[COMBO DANS CLUSTER : " + combo + "]";
    }

    @Override
    public float getPCombo() {
        return this.pCombo / cluster.getPCombo();
    }


    public void setMemeStrategieCluster() {
        this.combo.setStrategie(cluster.getStrategieActuelle());
    }
}
