package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.RequetesBDD;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClusteringSPRBTest {
    @Test
    void testPrincipal() {
        int nombreEntrees = 2000;
        List<Entree> resultats = recupererDonnees(nombreEntrees);
        int minEffectifCluster = 1000;
        List<ClusterSPRB> clustersHierarchiques = testHierarchical(resultats, minEffectifCluster);
        System.out.println("\n####RESULTATS CLUSTERING HIERARCHIQUE#####");
        testerResultats(clustersHierarchiques, minEffectifCluster);

        List<ClusterSPRB> clustersKmeans = testKmeans(resultats, minEffectifCluster);
        System.out.println("\n####RESULTATS KMEANS#####");
        testerResultats(clustersKmeans, minEffectifCluster);
    }

    private List<ClusterSPRB> testKmeans(List<Entree> resultats, int minEffectifCluster) {
        ClusteringSPRB clusteringSPRB = new ClusteringKMeansSPRB();
        clusteringSPRB.ajouterDonnees(resultats);

        return clusteringSPRB.construireClusters(minEffectifCluster);
    }

    List<ClusterSPRB> testHierarchical(List<Entree> resultats, int minEffectifCluster) {
        ClusteringSPRB clusteringSPRB = new ClusteringHierarchicalSPRB();
        clusteringSPRB.ajouterDonnees(resultats);

        return clusteringSPRB.construireClusters(minEffectifCluster);
    }

    private void testerResultats(List<ClusterSPRB> clusters, int minEffectifCluster) {
        assertNotNull(clusters, "Aucune valeur renvoyée par le clustering");

        int indexCluster = 0;
        for (ClusterSPRB cluster: clusters) {
            System.out.println("cluster  " + ++indexCluster);
            assertTrue(cluster.getEffectif() >= minEffectifCluster,
                    "Pas assez d'éléments dans le cluster : " + cluster.getEffectif());

            System.out.println("Taille cluster : " + cluster.getEffectif());
            System.out.println("Moyenne stack effectif : " + cluster.getEffectiveStack());
            System.out.println("Moyenne Pot : " + cluster.getPot());
            System.out.println("Pot bounty : " + cluster.getPotBounty());
        }
    }

    List<Entree> recupererDonnees(int nombreEntrees) {
        // on prend des entrées au hasard
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Entree> cq = cb.createQuery(Entree.class);
        Root<Entree> root = cq.from(Entree.class);
        cq.select(root);

        TypedQuery<Entree> query = session.createQuery(cq).setMaxResults(nombreEntrees);
        List<Entree> resultats = query.getResultList();

        RequetesBDD.fermerSession();
        return resultats;
    }

}
