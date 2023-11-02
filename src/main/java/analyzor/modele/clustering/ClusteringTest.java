package analyzor.modele.clustering;

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

        int nombreEntrees = 1000;
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Entree> cq = cb.createQuery(Entree.class);
        Root<Entree> root = cq.from(Entree.class);
        cq.select(root);

        TypedQuery<Entree> query = session.createQuery(cq).setMaxResults(nombreEntrees);
        List<Entree> resultats = query.getResultList();

        RequetesBDD.fermerSession();

        int minEffectifCluster = 10;
        ClusteringSPRB clusteringSPRB = new ClusteringSPRB(ClusteringHierarchique.MethodeLiaison.WARD);
        clusteringSPRB.ajouterDonnees(resultats);
        List<List<Entree>> clusters = clusteringSPRB.construireClusters(minEffectifCluster);

        assertNotNull(clusters, "Aucune valeur renvoyée par le clustering");

        for (List<Entree> cluster: clusters) {
            assertTrue(cluster.size() >= minEffectifCluster, "Pas assez d'éléments dans le cluster");
            float sommeSPR = 0;
            for (Entree entree : cluster) {
                sommeSPR += entree.getStackEffectif();
                //System.out.println(entree.getStackEffectif());
            }

            System.out.println("Taille cluster : " + cluster.size());
            System.out.println("Moyenne SPR : " + (sommeSPR / cluster.size()));
            System.out.println("Pot bounty : " + cluster.get(0).getPotBounty());
        }

    }

}
