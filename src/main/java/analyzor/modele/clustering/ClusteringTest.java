package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.GestionnaireFormat;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClusteringTest {
    @Test
    void testSPRB() {
        List<Entree> resultats = recupererDonneesArbre();
        int minEffectifCluster = 1000;
        List<ClusterSPRB> clustersHierarchiques = testHierarchicalSPRB(resultats, minEffectifCluster);
        System.out.println("\n####RESULTATS CLUSTERING HIERARCHIQUE#####");
        testerResultatsSPRB(clustersHierarchiques, minEffectifCluster);

        List<ClusterSPRB> clustersKmeans = testKmeansSPRB(resultats, minEffectifCluster);
        System.out.println("\n####RESULTATS KMEANS#####");
        testerResultatsSPRB(clustersKmeans, minEffectifCluster);
    }

    @Test
    void testBetSize() {
        List<Entree> resultats = recupererDonneesArbre();
        int minEffectifCluster = 100;

        System.out.println("\n####RESULTATS CLUSTERING HIERARCHIQUE#####");
        List<ClusterBetSize> clusterBetSizes = testHierarchicalBetSize(resultats, minEffectifCluster);
        testerResultatsBetSize(clusterBetSizes, minEffectifCluster);
    }

    private void testerResultatsBetSize(List<ClusterBetSize> clusterBetSizes, int minEffectifCluster) {
        assertNotNull(clusterBetSizes, "Aucune valeur renvoyée par le clustering");

        int sommeEffectifClusters = 0;
        for (ClusterBetSize cluster: clusterBetSizes) {
            sommeEffectifClusters += cluster.getEffectif();
        }

        int indexCluster = 0;
        for (ClusterBetSize cluster: clusterBetSizes) {
            System.out.println("cluster  " + ++indexCluster);
            // on ne teste ça que si l'échantillon est assez grand
            if (sommeEffectifClusters > minEffectifCluster)
                assertTrue(cluster.getEffectif() >= minEffectifCluster,
                        "Pas assez d'éléments dans le cluster : " + cluster.getEffectif());

            System.out.println("Taille cluster : " + cluster.getEffectif());
            System.out.println("Moyenne bet size : " + cluster.getBetSize());
        }
    }

    private List<ClusterBetSize> testHierarchicalBetSize(List<Entree> resultats, int minEffectifCluster) {
        HierarchicalBetSize clusteringHierarchique = new HierarchicalBetSize();
        clusteringHierarchique.ajouterDonnees(resultats);

        return clusteringHierarchique.construireClusters(minEffectifCluster);
    }

    private List<ClusterSPRB> testKmeansSPRB(List<Entree> resultats, int minEffectifCluster) {
        KMeansSPRB clusteringEntreeMinEffectif = new KMeansSPRB();
        clusteringEntreeMinEffectif.ajouterDonnees(resultats);

        return clusteringEntreeMinEffectif.construireClusters(minEffectifCluster);
    }

    List<ClusterSPRB> testHierarchicalSPRB(List<Entree> resultats, int minEffectifCluster) {
        HierarchiqueSPRB clusteringEntreeMinEffectif = new HierarchiqueSPRB();
        clusteringEntreeMinEffectif.ajouterDonnees(resultats);

        return clusteringEntreeMinEffectif.construireClusters(minEffectifCluster);
    }

    private void testerResultatsSPRB(List<ClusterSPRB> clusters, int minEffectifCluster) {
        assertNotNull(clusters, "Aucune valeur renvoyée par le clustering");

        int sommeEffectifClusters = 0;
        for (ClusterSPRB cluster: clusters) {
            sommeEffectifClusters += cluster.getEffectif();
        }

        int indexCluster = 0;
        for (ClusterSPRB cluster: clusters) {
            System.out.println("cluster  " + ++indexCluster);
            // on ne teste ça que si l'échantillon est assez grand
            if (sommeEffectifClusters > minEffectifCluster)
                assertTrue(cluster.getEffectif() >= minEffectifCluster,
                    "Pas assez d'éléments dans le cluster : " + cluster.getEffectif());

            System.out.println("Taille cluster : " + cluster.getEffectif());
            System.out.println("Moyenne stack effectif : " + cluster.getEffectiveStack());
            System.out.println("Moyenne Pot : " + cluster.getPot());
            System.out.println("Pot bounty : " + cluster.getPotBounty());
        }
    }

    List<Entree> recupererDonneesArbre() {
        Variante.PokerFormat pokerFormat = Variante.PokerFormat.SPIN;
        int nombreJoueurs = 3;
        TourMain.Round round = TourMain.Round.PREFLOP;


        // on prend des entrées de l'arbre au hasard
        FormatSolution formatSolution = new FormatSolution(pokerFormat, nombreJoueurs);
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        List<NoeudAbstrait> noeudsArbres = arbreAbstrait.obtenirNoeuds();

        List<Entree> toutesLesSituations = GestionnaireFormat.getEntrees(formatSolution, round);
        LinkedHashMap<NoeudAbstrait, List<Entree>> situationsGroupees = arbreAbstrait.trierEntrees(toutesLesSituations);

        Set<NoeudAbstrait> keys = situationsGroupees.keySet();
        List<NoeudAbstrait> listeCles = new ArrayList<>(keys);

        Random random = new Random();
        int indexRandom = random.nextInt(listeCles.size() - 1);
        NoeudAbstrait noeudRandom = listeCles.get(indexRandom);

        List<Entree> data = situationsGroupees.get(noeudRandom);
        System.out.println("Action clusterisee : " + noeudRandom);
        return data;
    }

}
