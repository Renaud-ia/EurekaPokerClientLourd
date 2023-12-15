package analyzor.modele.equilibrage;

import analyzor.modele.clustering.HierarchiqueEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboDenombrable;
import analyzor.modele.equilibrage.leafs.ProbaEquilibrage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * construit l'arbre puis transmet à l'équilibrateur l'équilibrage des leafs selon les valeurs renvoyées
 */
public class ArbreEquilibrage {
    private final Logger logger = LogManager.getLogger(ArbreEquilibrage.class);
    private static final float PCT_NOT_FOLDED = 0.5f;
    private final List<ComboDenombrable> leafs;
    private final int pas;
    private final List<ComboEquilibrage> noeuds;
    private final int nSituations;

    public ArbreEquilibrage(List<ComboDenombrable> comboDenombrables, int pas, int nSituations) {
        this.leafs = comboDenombrables;
        this.pas = pas;
        noeuds = new ArrayList<>();
        this.nSituations = nSituations;
    }

    public void equilibrer(float[] pActionsReelles, float pFoldReelle) {
        construireArbre(pFoldReelle);

        Equilibrateur equilibrateur = new Equilibrateur(noeuds, pActionsReelles, pFoldReelle);
        equilibrateur.lancerEquilibrage();
    }

    private void construireArbre(float pFoldReelle) {
        HierarchiqueEquilibrage clustering = new HierarchiqueEquilibrage(nSituations);
        ProbaEquilibrage probaEquilibrage = new ProbaEquilibrage(nSituations, this.pas);

        // on crée simplement un noeud par combo
        // on calcule les probas
        float pRangeAjoutee = 0;
        float notFolded = (1 - pFoldReelle) * PCT_NOT_FOLDED;

        List<ComboEquilibrage> combosAsNoeuds = new ArrayList<>();
        for (ComboDenombrable comboDenombrable : leafs) {
            ComboEquilibrage comboNoeud = new ComboEquilibrage(comboDenombrable);
            logger.trace(comboNoeud + " sera pas foldé : " + (pRangeAjoutee < notFolded));
            comboNoeud.setPas(pas);
            // les combos sont triés par ordre d'équité en amont
            comboNoeud.setNotFolded(pRangeAjoutee < notFolded);
            probaEquilibrage.calculerProbas(comboNoeud);
            comboNoeud.setStrategiePlusProbable();
            combosAsNoeuds.add(comboNoeud);
            pRangeAjoutee += comboDenombrable.getPCombo();
            logger.trace(Arrays.toString(comboNoeud.getStrategie()));
        }

        clustering.ajouterDonnees(combosAsNoeuds);
        clustering.lancerClustering();
        List<ComboEquilibrage> clusters = clustering.getResultats();

        for (ComboEquilibrage cluster : clusters) {
            loggerCluster(cluster);
            cluster.setPas(pas);
            probaEquilibrage.calculerProbas(cluster);
            cluster.initialiserStrategie();
            noeuds.add(cluster);
        }
    }

    private void loggerCluster(ComboEquilibrage cluster) {
        StringBuilder stringCluster = new StringBuilder();
        stringCluster.append("CLUSTER FORME : [");

        for (ComboDenombrable comboDenombrable : cluster.getCombosDenombrables()) {
            stringCluster.append(comboDenombrable);
            stringCluster.append(", ");
        }
        stringCluster.append("]");
        logger.trace(stringCluster.toString());
    }

}
