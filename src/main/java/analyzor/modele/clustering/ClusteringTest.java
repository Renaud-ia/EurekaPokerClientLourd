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

public class ClusteringTest {
    @Test
    void testSPRB() {
        // on prend des entrées au hasard
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();

        int nombreEntrees = 2000;
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Entree> cq = cb.createQuery(Entree.class);
        Root<Entree> root = cq.from(Entree.class);
        cq.select(root);

        TypedQuery<Entree> query = session.createQuery(cq).setMaxResults(nombreEntrees);
        List<Entree> resultats = query.getResultList();

        RequetesBDD.fermerSession();

        int minEffectifCluster = 100;
        ClusteringSPRB clusteringSPRB = new ClusteringSPRB();
        clusteringSPRB.ajouterDonnees(resultats);
        List<ClusterSPRB> clusters = clusteringSPRB.construireClusters(minEffectifCluster);

        assertNotNull(clusters, "Aucune valeur renvoyée par le clustering");

        int inferieur10bb = 0;
        int superieur30bb = 0;
        for (ClusterSPRB cluster: clusters) {
            assertTrue(cluster.getEffectif() >= minEffectifCluster, "Pas assez d'éléments dans le cluster");

            System.out.println("Taille cluster : " + cluster.getEffectif());
            System.out.println("Moyenne stack effectif : " + cluster.getEffectiveStack());
            System.out.println("Moyenne Pot : " + cluster.getPot());
            System.out.println("Pot bounty : " + cluster.getPotBounty());
        }

        System.out.println("Inférieur à 10bb : " + inferieur10bb);
        System.out.println("Supérieur à 30bb : " + superieur30bb);

    }

}
