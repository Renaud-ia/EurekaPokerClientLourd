package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.parties.Entree;

import java.util.List;

public interface ClusteringSPRB {
    public void ajouterDonnees(List<Entree> donneesEntrees);
    public List<ClusterSPRB> construireClusters(int minimumPoints);
}
