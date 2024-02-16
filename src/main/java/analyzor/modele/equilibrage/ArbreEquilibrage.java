package analyzor.modele.equilibrage;

import analyzor.modele.clustering.HierarchiqueRange;
import analyzor.modele.clustering.SpecialRange;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboDansCluster;
import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * construit l'arbre puis transmet à l'équilibrateur l'équilibrage des leafs selon les valeurs renvoyées
 * fixe les probabilités de fold liées à l'équité
 */
public class ArbreEquilibrage {
    private final Logger logger = LogManager.getLogger(ArbreEquilibrage.class);
    private final static int N_PROCESSEURS = Runtime.getRuntime().availableProcessors() - 2;
    private final List<ComboDenombrable> leafs;
    private final List<ComboIsole> comboIsoles;
    private final int nSituations;
    private final Float pFold;
    private final ProbaFold probaFold;
    private final int pas;

    public ArbreEquilibrage(List<ComboDenombrable> comboDenombrables, int pas, int nSituations, Float pFold) {
        this.leafs = comboDenombrables;
        comboIsoles = new ArrayList<>();

        this.nSituations = nSituations;
        this.pFold = pFold;
        this.pas = pas;

        probaFold = new ProbaFold(pas);

        ProbaObservations.setPas(pas);
        ProbaObservations.setNombreSituations(nSituations);
    }

    public void equilibrer(float[] pActionsReelles) {
        if (pActionsReelles.length == 1) {
            logger.error("Une seule action détectée");
            remplirStrategieUnique();
            return;
        }

        List<ClusterEquilibrage> clusters = construireClusters();
        initialiserClusters(clusters);
        equilibrer(clusters, pActionsReelles);
        equilibrerCombosDansClusters(clusters);
    }

    // méthodes privées

    /**
     * procédure où cas où on a une seule action
     */
    private void remplirStrategieUnique() {
        for (ComboDenombrable combo : leafs) {
            combo.setStrategieUnique();
        }
    }

    /**
     * méthode de construction des clusters
     */
    private List<ClusterEquilibrage> construireClusters() {
        // on vérifie que fold fait partie des actions possibles ou non
        // sinon va change les probas
        float pFoldReelle = verifierFold();

        // on calcule les probas pour chaque leaf
        calculerProbasLeafs();
        logger.trace("Calcul terminé");

        // on définit une probabilité de fold
        probaFold.estimerProbaFold(nSituations, pFoldReelle, comboIsoles);

        // on convertit les combos au bon format pour clustering
        // todo : à quoi ça sert que le clustering prenne plusieurs types d'objets équilibrables???
        List<NoeudEquilibrage> combosAsNoeuds = new ArrayList<>(comboIsoles);

        // on clusterise la range
        HierarchiqueRange clustering = new HierarchiqueRange(nSituations);
        clustering.ajouterDonnees(combosAsNoeuds);
        clustering.lancerClustering();

        return clustering.getResultats();
    }

    /**
     * méthode pour calculer les probabilités des combos isolés
     */
    private void calculerProbasLeafs() {
        logger.trace("Début du calcul multiprocessé de probabilités");

        try (ExecutorService executorService = Executors.newFixedThreadPool(N_PROCESSEURS)) {
            // Pour chaque ComboDenombrable dans leafs, soumettre une tâche à l'ExecutorService
            for (ComboDenombrable comboDenombrable : leafs) {
                logger.trace("Calcul de proba lancé pour : " + comboDenombrable);
                ComboIsole comboNoeud = new ComboIsole(comboDenombrable);
                comboIsoles.add(comboNoeud);
                ProbaObservations probaObservations = new ProbaObservations(comboNoeud);
                executorService.submit(probaObservations);
            }
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(20, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
        catch (Exception e) {
            logger.error("Calcul de probabiltités interrompu", e);
        }
    }

    /**
     * on prépare les clusters pour l'équilibrage
     * @param clusters clusters calculés précédemment
     */
    private void initialiserClusters(List<ClusterEquilibrage> clusters) {
        for (ClusterEquilibrage cluster : clusters) {
            logger.trace(cluster);
            ProbaObservations probaObservations = new ProbaObservations(cluster);
            probaObservations.run();
            cluster.initialiserStrategie(pas);
            cluster.setStrategiePlusProbable();

            for (ComboDansCluster comboDansCluster : cluster.getCombos()) {
                comboDansCluster.initialiserStrategie(pas);
                //comboDansCluster.setStrategiePlusProbable();
            }
        }
    }

    /**
     * équilibrage des Combos individuels au sein des clusters déjà équilibrés
     */
    private void equilibrerCombosDansClusters(List<ClusterEquilibrage> clusters) {
        for (ClusterEquilibrage clusterEquilibrage : clusters) {
            List<ComboDansCluster> combosDansClusters = clusterEquilibrage.getCombos();
            float[] pActionsCluster = clusterEquilibrage.getStrategieActuelle();
            // todo pour test, on même la même stratégie partout
            //equilibrer(combosDansClusters, pActionsCluster);

            // on répercute la stratégie dans le combo dénombrable correspondant
            for (ComboDansCluster combo : combosDansClusters) {
                // todo pour test, on même la même stratégie partout
                combo.setMemeStrategieCluster();
                //combo.fixerStrategie();
            }
        }
    }

    /**
     * méthode pour équilibrer n'importe quel type d'objet équilibrable (combo dans cluster ou combo isolé)
     * @param noeuds noeuds à équilibrer (doivent être initialisés)
     * @param pActionsReelles actions constatées
     */
    private void equilibrer(List<? extends NoeudEquilibrage> noeuds, float[] pActionsReelles) {
        Equilibrateur equilibrateur = new Equilibrateur(noeuds, pActionsReelles);
        equilibrateur.lancerEquilibrage();
    }

    /**
     * méthode pour vérifier si le fold est possible
     * @return la valeur de pFold (= 0 si pas de fold)
     */
    private float verifierFold() {
        float pFoldReel = 0;
        if (pFold == null) {
            ProbaObservations.setFoldPossible(false);
            pFoldReel = 0;
        }
        else {
            ProbaObservations.setFoldPossible(true);
            pFoldReel = pFold;
        }
        return pFoldReel;
    }

    // on a fixé les stratégies
    public List<ComboDenombrable> getCombosEquilibres() {
        return leafs;
    }

}
