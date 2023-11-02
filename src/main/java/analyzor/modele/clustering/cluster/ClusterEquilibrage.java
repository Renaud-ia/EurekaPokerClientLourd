package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.ClusteringEquilibrage;

import java.util.List;

public class ClusterEquilibrage extends ClusterHierarchique {
    private ClusteringEquilibrage parent;
    private ClusteringEquilibrage enfant;
    ClusterEquilibrage(List<ObjetClusterisable> objetDepart, int indexCluster) {
        super(objetDepart, indexCluster);
    }


}
