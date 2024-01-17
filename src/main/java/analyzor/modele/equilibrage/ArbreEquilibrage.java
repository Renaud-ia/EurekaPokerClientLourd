package analyzor.modele.equilibrage;

import analyzor.modele.clustering.HierarchiqueEquilibrage;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboDansCluster;
import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
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
    private final int nSituations;
    private final Float pFold;

    public ArbreEquilibrage(List<ComboDenombrable> comboDenombrables, int pas, int nSituations, Float pFold) {
        this.leafs = comboDenombrables;
        this.pas = pas;
        this.nSituations = nSituations;
        this.pFold = pFold;
    }

    public void equilibrer(float[] pActionsReelles) {
        if (pActionsReelles.length == 1) {
            logger.error("Une seule action détectée");
            remplirStrategieUnique();
            return;
        }

        List<ClusterEquilibrage> clusters = construireClusters();
        equilibrer(clusters, pActionsReelles);

        for (ClusterEquilibrage clusterEquilibrage : clusters) {
            List<ComboDansCluster> combosDansClusters = clusterEquilibrage.getCombos();
            float[] pActionsCluster = clusterEquilibrage.getStrategieActuelle();
            equilibrer(combosDansClusters, pActionsCluster);

            // on répercute la stratégie dans le combo dénombrable correspondant
            for (ComboDansCluster combo : combosDansClusters) {
                combo.fixerStrategie();
            }
        }

    }

    private void remplirStrategieUnique() {
        for (ComboDenombrable combo : leafs) {
            combo.setStrategieUnique();
        }
    }

    private List<ClusterEquilibrage> construireClusters() {
        boolean foldPossible;
        float pFoldReelle;
        if (pFold == null) {
            foldPossible = false;
            pFoldReelle = 0;
        }
        else {
            foldPossible = true;
            pFoldReelle = pFold;
        }

        List<ClusterEquilibrage> noeuds = new ArrayList<>();
        HierarchiqueEquilibrage clustering = new HierarchiqueEquilibrage(nSituations);
        ProbaEquilibrage probaEquilibrage = new ProbaEquilibrage(nSituations, this.pas);

        // on crée simplement un noeud par combo
        // on calcule les probas
        float pRangeAjoutee = 0;
        float notFolded = (1 - pFoldReelle) * PCT_NOT_FOLDED;

        List<NoeudEquilibrage> combosAsNoeuds = new ArrayList<>();
        for (ComboDenombrable comboDenombrable : leafs) {
            // la liste va garder le type d'origine
            ComboIsole comboNoeud = new ComboIsole(comboDenombrable);
            logger.trace(comboNoeud + " sera pas foldé : " + (pRangeAjoutee < notFolded));
            // les combos sont triés par ordre d'équité en amont
            comboNoeud.setNotFolded(pRangeAjoutee < notFolded);
            probaEquilibrage.calculerProbas(comboNoeud, foldPossible);
            comboNoeud.initialiserStrategie();
            combosAsNoeuds.add(comboNoeud);
            pRangeAjoutee += comboDenombrable.getPCombo();
            logger.trace(Arrays.toString(comboNoeud.getStrategieActuelle()));
        }

        clustering.ajouterDonnees(combosAsNoeuds);
        clustering.lancerClustering();
        List<ClusterEquilibrage> clusters = clustering.getResultats();

        for (ClusterEquilibrage cluster : clusters) {
            logger.trace(cluster);
            probaEquilibrage.calculerProbas(cluster, foldPossible);
            cluster.initialiserStrategie();
            noeuds.add(cluster);
        }

        return noeuds;
    }

    private void equilibrer(List<? extends NoeudEquilibrage> noeuds, float[] pActionsReelles) {
        Equilibrateur equilibrateur = new Equilibrateur(noeuds, pActionsReelles);
        equilibrateur.lancerEquilibrage();
    }

    // on a fixé les stratégies
    public List<ComboDenombrable> getCombosEquilibres() {
        return leafs;
    }

}
