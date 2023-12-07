package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

public class DistanceCluster<T extends ObjetClusterisable> {
    private long index;
    private ClusterFusionnable<T> cluster1;
    private ClusterFusionnable<T> cluster2;
    private float distance;

    public DistanceCluster(ClusterFusionnable<T> cluster1, ClusterFusionnable<T> cluster2,
                           float distance, long index) {
        if (cluster1 == null || cluster2 == null) {
            throw new IllegalArgumentException("Un des clusters est nul");
        }
        this.index = index;

        this.cluster1 = cluster1;
        this.cluster2 = cluster2;
        this.distance = distance;
    }

    public float getDistance() {
        return distance;
    }

    public ClusterFusionnable<T> getPremierCluster() {
        return cluster1;
    }

    public ClusterFusionnable<T> getSecondCluster() {
        return cluster2;
    }

    public long getIndex() {
        return index;
    }

    public void setPremierCluster(ClusterFusionnable<T> clusterFusionne) {
        this.cluster1 = clusterFusionne;
    }

    public void setSecondCluster(ClusterFusionnable<T> clusterFusionne) {
        this.cluster2 = clusterFusionne;
    }

    public boolean contient(ClusterFusionnable<T> autreCluster) {
        return cluster1 == autreCluster || cluster2 == autreCluster;
    }

    public void setDistance(float nouvelleDistance) {
        this.distance = nouvelleDistance;
    }

    public void modifierCluster(ClusterFusionnable<T> clusterModifie, ClusterFusionnable<T> clusterFusionne) {
        if (cluster1 == clusterModifie) {
            cluster1 = clusterFusionne;
        }
        else if (cluster2 == clusterModifie) {
            cluster2 = clusterFusionne;
        }
        else{
            System.out.println("Index du cluster 1 : " + cluster1.getIndex() + " objets :" + cluster1.getEffectif());
            System.out.println("Index du cluster 2 : " + cluster2.getIndex() + " objets :" + cluster2.getEffectif());
            System.out.println("Index du cluster modifié : " + clusterModifie.getIndex() + " objets :" + clusterModifie.getEffectif());
            throw new IllegalArgumentException("Le cluster n'existe pas dans la paire concernée");
        }
    }
}
