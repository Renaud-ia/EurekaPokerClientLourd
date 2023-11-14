package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterEntree;
import analyzor.modele.parties.Entree;

import java.util.List;

public interface ClusteringEntreeMinEffectif {
    public void ajouterDonnees(List<Entree> donneesEntrees);
    public List<? extends ClusterEntree> construireClusters(int minimumPoints);
}
