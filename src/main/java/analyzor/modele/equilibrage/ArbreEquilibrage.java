package analyzor.modele.equilibrage;

import analyzor.modele.clustering.KMeansEquilibrage;
import analyzor.modele.clustering.algos.ClusteringHierarchique;
import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterHierarchique;
import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.equilibrage.leafs.ComboDenombrable;
import analyzor.modele.equilibrage.leafs.ProbaEquilibrage;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * construit l'arbre puis transmet à l'équilibrateur l'équilibrage des leafs selon les valeurs renvoyées
 */
public class ArbreEquilibrage {
    private final Logger logger = LogManager.getLogger(ArbreEquilibrage.class);
    private final List<ComboDenombrable> leafs;
    private final int pas;
    private final List<NoeudEquilibrage> noeuds;
    private final int nSituations;

    public ArbreEquilibrage(List<ComboDenombrable> comboDenombrables, int pas, int nSituations) {
        this.leafs = comboDenombrables;
        this.pas = pas;
        noeuds = new ArrayList<>();
        this.nSituations = nSituations;
    }

    public void equilibrer(float[] pActionsReelles, float pFoldReelle) {
        construireArbre();

        Equilibrateur equilibrateur = new Equilibrateur(noeuds, pActionsReelles, pFoldReelle);
        equilibrateur.lancerEquilibrage();
    }

    private void construireArbre() {
        KMeansEquilibrage clustering = new KMeansEquilibrage();
        ProbaEquilibrage probaEquilibrage = new ProbaEquilibrage(nSituations, this.pas);

        // on crée simplement un noeud par combo
        // on calcule les probas
        List<NoeudEquilibrage> combosAsNoeuds = new ArrayList<>();
        for (ComboDenombrable comboDenombrable : leafs) {
            NoeudEquilibrage comboNoeud = new NoeudEquilibrage(comboDenombrable);
            comboNoeud.setPas(pas);
            probaEquilibrage.calculerProbas(comboNoeud);
            comboNoeud.setStrategiePlusProbable();
            combosAsNoeuds.add(comboNoeud);
        }

        clustering.ajouterDonnees(combosAsNoeuds);
        clustering.lancerClustering();
        List<List<ComboDenombrable>> clusters = clustering.getResultats();

        for (List<ComboDenombrable> cluster : clusters) {
            loggerCluster(cluster);
            NoeudEquilibrage noeudEquilibrage = new NoeudEquilibrage(cluster);
            noeudEquilibrage.setPas(pas);
            probaEquilibrage.calculerProbas(noeudEquilibrage);
            noeudEquilibrage.initialiserStrategie();
            noeuds.add(noeudEquilibrage);
        }
    }

    private void loggerCluster(List<ComboDenombrable> cluster) {
        StringBuilder stringCluster = new StringBuilder();
        stringCluster.append("CLUSTER FORME : [");

        for (ComboDenombrable comboDenombrable : cluster) {
            stringCluster.append(comboDenombrable);
            stringCluster.append(", ");
        }
        stringCluster.append("]");
        logger.trace(stringCluster.toString());
    }

}
