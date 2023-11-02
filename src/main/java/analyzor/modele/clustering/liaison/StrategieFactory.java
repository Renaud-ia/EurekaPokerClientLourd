package analyzor.modele.clustering.liaison;

import analyzor.modele.clustering.ClusteringHierarchique;
import analyzor.modele.clustering.StrategieLiaison;
import analyzor.modele.clustering.liaison.LiaisonCentree;
import analyzor.modele.clustering.liaison.LiaisonWard;

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
