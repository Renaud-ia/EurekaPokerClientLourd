package analyzor.modele.clustering;

import analyzor.modele.clustering.objets.EntreeSPRB;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public class ClusteringSimpleSPRB {
    private List<EntreeSPRB> listeEntrees;
    private float minSPR;
    private float maxSPR;
    private float minBounty;
    private float maxBounty;
    public ClusteringSimpleSPRB() {
        listeEntrees = new ArrayList<>();
    }
    public void ajouterDonnees(List<Entree> donneesEntrees) {

        for (Entree entree : donneesEntrees) {
            EntreeSPRB entreeSPRB = new EntreeSPRB(entree);
            listeEntrees.add(entreeSPRB);
        }
    }

    public List<List<Entree>> construireClusters(int minimumCluster) {


        return new ArrayList<>();
    }
}
