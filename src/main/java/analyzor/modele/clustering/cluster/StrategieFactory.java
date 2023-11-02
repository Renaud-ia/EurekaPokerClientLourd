package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.ClusteringHierarchique;

public class StrategieFactory {
    public static StrategieLiaison getStrategie(ClusteringHierarchique.MethodeLiaison methodeLiaison) {
        if (methodeLiaison == ClusteringHierarchique.MethodeLiaison.CENTREE) {
            return new LiaisonCentree();
        }
        else if (methodeLiaison == ClusteringHierarchique.MethodeLiaison.WARD) {
            return new LiaisonWard();
        }
        else throw new IllegalArgumentException("Méthode de liaison invalide");
    }
}
