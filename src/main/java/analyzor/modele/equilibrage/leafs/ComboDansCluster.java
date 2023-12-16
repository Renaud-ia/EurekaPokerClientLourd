package analyzor.modele.equilibrage.leafs;

import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.Strategie;

/**
 * va prendre en compte les combos voisins pour l'équilibrage
 * et aussi limitation par la stratégie du cluster
 */
public class ComboDansCluster extends ComboIsole {
    private final ClusterEquilibrage cluster;

    protected ComboDansCluster(ComboDenombrable comboDenombrable, ClusterEquilibrage cluster) {
        super(comboDenombrable);
        this.cluster = cluster;
    }

    // va prendre en compte les probabilités de changement des autres clusters
    @Override
    protected float probabiliteChangement(Strategie strategie, int indexAction, int sensChangement) {
        //todo
        return 0f;
    }

    @Override
    public String toString() {
        return "[COMBO DANS CLUSTER : " + combo + "]";
    }
}
