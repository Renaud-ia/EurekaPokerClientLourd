package analyzor.modele.equilibrage;

import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.equilibrage.leafs.ComboDenombrable;
import analyzor.modele.equilibrage.leafs.ProbaEquilibrage;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * construit l'arbre puis gère l'équilibrage des leafs selon les valeurs renvoyées
 * apporte de la randomisation
 * détecte les blocages et agit en conséquence
 */
public class Equilibrateur {
    // todo est ce la meilleure méthode de liaison?
    private final ClusteringHierarchique.MethodeLiaison METHODE_LIAISON = ClusteringHierarchique.MethodeLiaison.WARD;
    private final List<ComboDenombrable> leafs;
    private final int pas;
    private final RegressionEquilibrage regressionEquilibrage;
    public Equilibrateur(List<ComboDenombrable> comboDenombrables, int pas) {
        this.leafs = comboDenombrables;
        this.pas = pas;
        // un clustering hiérarchique va donner N-1 itérations (= N-1 clusters)
        int nombreClusters = comboDenombrables.size() - 1;
        this.regressionEquilibrage = new RegressionEquilibrage(nombreClusters);
    }

    public void initialiserProbas(int nSituations) {
        ProbaEquilibrage probaEquilibrage = new ProbaEquilibrage(nSituations, this.pas);
        for (ComboDenombrable comboDenombrable : leafs) {
            probaEquilibrage.calculerProbas(comboDenombrable);
            comboDenombrable.initialiserStrategie();
        }
    }

    public void construireArbre() {
        ClusteringHierarchique<ComboDenombrable> clustering =
                new ClusteringHierarchique<>(METHODE_LIAISON);

        HashMap<Integer, Enfant> tableNoeuds = new HashMap<>();

        int index = 0;
        List<ClusterHierarchique<ComboDenombrable>> clusters = new ArrayList<>();
        for (ComboDenombrable comboDenombrable : leafs) {
            ClusterHierarchique<ComboDenombrable> clusterHierarchique =
                    new ClusterHierarchique<>(comboDenombrable, index);
            clusters.add(clusterHierarchique);
            tableNoeuds.put(index, comboDenombrable);
            index++;
        }
        clustering.ajouterClusters(clusters);

        Pair<Integer, Integer> clustersFusionnes;
        int compte = 0;
        // todo : faut-il limiter la profondeur de l'arbre ??
        while ((clustersFusionnes = clustering.indexFusionnes()) != null) {
            int indexEnfant1 = clustersFusionnes.getFirst();
            int indexEnfant2 = clustersFusionnes.getSecond();
            Enfant enfant1 = tableNoeuds.get(indexEnfant1);
            Enfant enfant2 = tableNoeuds.get(indexEnfant2);
            if (enfant1 == null || enfant2 == null) throw new RuntimeException("Index de l'enfant non trouvé");

            // on ne garde pas les références au noeud car on ne va utiliser que les leafs pour l'équilibrage
            System.out.println("Noeud d'équilibrage d'index : " + compte);
            NoeudEquilibrage noeudEquilibrage = new NoeudEquilibrage(regressionEquilibrage, enfant1, enfant2, compte++);

            // on change les index car ils sont répercutés dans le clustering
            tableNoeuds.put(indexEnfant1, noeudEquilibrage);
            tableNoeuds.put(indexEnfant2, noeudEquilibrage);
        }

    }

    public void equilibrer(float[] pActionsReelles, float pFoldReelle) {
        float[] pActionsEstimees = frequencesAction();
        float pFoldEstimee = frequenceFold();


    }

    private float frequenceFold() {
        return 0f;
    }

    private float[] frequencesAction() {
        return null;
    }
}
