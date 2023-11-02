package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.ClusteringHierarchique;
import analyzor.modele.clustering.objets.ObjetClusterisable;

public class StrategieFactory<T extends ObjetClusterisable> {
    private final ClusteringHierarchique.MethodeLiaison methodeLiaison;

    public StrategieFactory(ClusteringHierarchique.MethodeLiaison methodeLiaison) {
        this.methodeLiaison = methodeLiaison;
    }

    public StrategieLiaison<T> getStrategie() {
        if (methodeLiaison == ClusteringHierarchique.MethodeLiaison.CENTREE) {
            return new LiaisonCentree<>();
        }
        else if (methodeLiaison == ClusteringHierarchique.MethodeLiaison.WARD) {
            return new LiaisonWard<>();
        }
        else if (methodeLiaison == ClusteringHierarchique.MethodeLiaison.MEDIANE) {
            return new LiaisonMediane<>();
        }
        else if (methodeLiaison == ClusteringHierarchique.MethodeLiaison.MOYENNE) {
            return new LiaisonMoyenne<>();
        }
        else if(methodeLiaison == ClusteringHierarchique.MethodeLiaison.SIMPLE) {
            return new LiaisonSimple<>();
        }
        else if(methodeLiaison == ClusteringHierarchique.MethodeLiaison.COMPLETE) {
            return new LiaisonComplete<>();
        }
        else throw new IllegalArgumentException("Méthode de liaison invalide");
    }
}
