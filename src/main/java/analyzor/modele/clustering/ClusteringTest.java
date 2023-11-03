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

        int nombreEntrees = 2000;
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Entree> cq = cb.createQuery(Entree.class);
        Root<Entree> root = cq.from(Entree.class);
        cq.select(root);

        TypedQuery<Entree> query = session.createQuery(cq).setMaxResults(nombreEntrees);
        List<Entree> resultats = query.getResultList();

        RequetesBDD.fermerSession();

        int minEffectifCluster = 100;
        ClusteringHierarchiqueSPRB clusteringHierarchiqueSPRB = new ClusteringHierarchiqueSPRB(ClusteringHierarchique.MethodeLiaison.CENTREE);
        clusteringHierarchiqueSPRB.ajouterDonnees(resultats);
        List<List<Entree>> clusters = clusteringHierarchiqueSPRB.construireClusters(minEffectifCluster);

        assertNotNull(clusters, "Aucune valeur renvoyée par le clustering");

        int inferieur10bb = 0;
        int superieur30bb = 0;
        for (List<Entree> cluster: clusters) {
            assertTrue(cluster.size() >= minEffectifCluster, "Pas assez d'éléments dans le cluster");
            float sommeSPR = 0;
            for (Entree entree : cluster) {
                sommeSPR += entree.getStackEffectif();
                if (entree.getStackEffectif() < 10) inferieur10bb++;
                if (entree.getStackEffectif() > 30) superieur30bb++;
            }

            System.out.println("Taille cluster : " + cluster.size());
            System.out.println("Moyenne SPR : " + (sommeSPR / cluster.size()));
            System.out.println("Pot bounty : " + cluster.get(0).getPotBounty());
        }

        System.out.println("Inférieur à 10bb : " + inferieur10bb);
        System.out.println("Supérieur à 30bb : " + superieur30bb);

    }

}
