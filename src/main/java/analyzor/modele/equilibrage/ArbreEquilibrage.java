package analyzor.modele.equilibrage;

import analyzor.modele.clustering.HierarchiqueEquilibrage;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
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
    private final List<NoeudEquilibrage> noeuds;
    private final int nSituations;

    public ArbreEquilibrage(List<ComboDenombrable> comboDenombrables, int pas, int nSituations) {
        this.leafs = comboDenombrables;
        this.pas = pas;
        noeuds = new ArrayList<>();
        this.nSituations = nSituations;
    }

    public void equilibrer(float[] pActionsReelles) {
        construireArbre(pActionsReelles[pActionsReelles.length - 1]);

        Equilibrateur equilibrateur = new Equilibrateur(noeuds, pActionsReelles);
        equilibrateur.lancerEquilibrage();
    }

    private void construireArbre(float pFoldReelle) {
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
            probaEquilibrage.calculerProbas(comboNoeud);
            comboNoeud.initialiserStrategie();
            combosAsNoeuds.add(comboNoeud);
            pRangeAjoutee += comboDenombrable.getPCombo();
            logger.trace(Arrays.toString(comboNoeud.getStrategieActuelle()));
        }

        clustering.ajouterDonnees(combosAsNoeuds);
        clustering.lancerClustering();
        List<ClusterEquilibrage> clusters = clustering.getResultats();

        for (NoeudEquilibrage cluster : clusters) {
            loggerCluster(cluster);
            probaEquilibrage.calculerProbas(cluster);
            cluster.initialiserStrategie();
            noeuds.add(cluster);
        }
    }

    private void loggerCluster(NoeudEquilibrage cluster) {
        logger.trace(cluster);
    }

}
