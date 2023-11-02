package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.ClusteringEquilibrage;
import analyzor.modele.clustering.objets.ObjetClusterisable;

public class ClusterEquilibrage extends ClusterHierarchique {
    //todo
    private ClusteringEquilibrage parent;
    private ClusteringEquilibrage enfant;


    public ClusterEquilibrage(ObjetClusterisable objetDepart, int indexCluster) {
        super(objetDepart, indexCluster);
    }
}
