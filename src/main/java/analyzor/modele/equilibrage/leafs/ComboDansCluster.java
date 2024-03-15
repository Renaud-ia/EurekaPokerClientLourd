package analyzor.modele.equilibrage.leafs;

import java.util.HashMap;

/**
 * va prendre en compte les combos voisins pour l'équilibrage
 * et aussi limitation par la stratégie du cluster
 * todo : revoir complètement l'initialisation de la stratégie
 */
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
    }

    /**
     * va prendre en compte les probabilités de changement des autres clusters à partir de la même stratégie
     * ProbaFinale = (probaPropre + β (∑i f(distance_i) * probaInterne(i))) / (1 + β)
      */
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

    /**
     * renvoie la proba interne d'un cluster mais selon une valeur fixée indépendante de sa stratégie
     * utile seulement pour équilibrage des combos dans Cluster
     */
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

    /**
     * fonction décroissante qui map les distances
     */
    private float transformationDistance(float distance) {
        return (float) (1 / Math.pow(distance, ACCENTUATION_DISTANCE));
    }

    // todo PRODUCTION à supprimer
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
