package analyzor.modele.clustering;

import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public class ClusteringSRPB extends ClusteringHierarchique {

    public ClusteringSRPB(ClusteringHierarchique.MethodeLiaison methodeLiaison) {
        super(methodeLiaison);
    }

    public void ajouterDonnees(List<Entree> donneesEntrees) {

    }

    public List<List<Entree>> construireClusters(int minimumCluster) {
        return new ArrayList<>();
    }
}
