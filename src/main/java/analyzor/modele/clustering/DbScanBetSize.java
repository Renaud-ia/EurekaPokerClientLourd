package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.DBScan;
import analyzor.modele.clustering.cluster.*;
import analyzor.modele.clustering.objets.EntreeBetSize;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public class DbScanBetSize extends DBScan<EntreeBetSize> {
    // todo trouver la bonne valeur epsilon
    private static final float epsilon = 0.01f;
    public DbScanBetSize() {
        super(epsilon);
    }


    public void ajouterDonnees(List<Entree> donneesEntrees) {
        List<EntreeBetSize> donneesTransformees = new ArrayList<>();
        for (Entree entree : donneesEntrees) {
            EntreeBetSize entreeSPRB = new EntreeBetSize(entree);
            donneesTransformees.add(entreeSPRB);
        }
        super.construireDonnees(donneesTransformees);
    }

    public List<ClusterBetSize> construireClusters(int minimumPoints) {
        this.setMinPoints(minimumPoints);
        clusteriserDonnees();

        List<ClusterDBSCAN<EntreeBetSize>> clusters = this.clusters;
        List<ClusterBetSize> resultats = new ArrayList<>();

        for (ClusterDBSCAN<EntreeBetSize> clusterHierarchique : clusters) {
            ClusterBetSize clusterBetSize = new ClusterBetSize();
            for (EntreeBetSize entreeBetSize : clusterHierarchique.getObjets()) {
                clusterBetSize.ajouterEntree(entreeBetSize.getEntree());
            }
            clusterBetSize.setBetSize(clusterHierarchique.getCentroide()[0]);
            resultats.add(clusterBetSize);
        }

        return resultats;
    }
}
