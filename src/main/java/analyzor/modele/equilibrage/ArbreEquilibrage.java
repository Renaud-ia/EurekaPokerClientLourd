package analyzor.modele.equilibrage;

import analyzor.modele.clustering.range.ClusteringDivisifRange;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboDansCluster;
import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import analyzor.modele.estimation.CalculInterrompu;
import analyzor.modele.estimation.Estimateur;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ArbreEquilibrage {
    private final int N_PROCESSEURS;
    private final List<ComboDenombrable> leafs;
    private final List<ComboIsole> comboIsoles;
    private final int nSituations;
    private final Float pFold;
    private final ProbaFold probaFold;
    private final int pas;

    public ArbreEquilibrage(List<ComboDenombrable> comboDenombrables, int pas, int nSituations, Float pFold) {
        N_PROCESSEURS = Runtime.getRuntime().availableProcessors() - 3;
        this.leafs = comboDenombrables;
        comboIsoles = new ArrayList<>();

        this.nSituations = nSituations;
        this.pFold = pFold;
        this.pas = pas;

        probaFold = new ProbaFold(pas);

        ProbaObservations.setPas(pas);
        ProbaObservations.setNombreSituations(nSituations);
    }

    public void equilibrer(float[] pActionsReelles) throws CalculInterrompu {
        if (pActionsReelles.length == 1) {
            remplirStrategieUnique();
            return;
        }

        List<ClusterEquilibrage> clusters = construireClusters();
        initialiserClusters(clusters);
        equilibrer(clusters, pActionsReelles);
        equilibrerCombosDansClusters(clusters);
    }

    

    
    private void remplirStrategieUnique() {
        for (ComboDenombrable combo : leafs) {
            combo.setStrategieUnique();
        }
    }

    
    private List<ClusterEquilibrage> construireClusters() throws CalculInterrompu {
        
        
        float pFoldReelle = verifierFold();

        
        calculerProbasLeafs();

        
        probaFold.estimerProbaFold(nSituations, pFoldReelle, comboIsoles);

        
        
        List<NoeudEquilibrage> combosAsNoeuds = new ArrayList<>(comboIsoles);

        
        ClusteringDivisifRange clustering = new ClusteringDivisifRange(nSituations);
        clustering.ajouterDonnees(combosAsNoeuds);

        return clustering.getResultats();
    }

    
    private void calculerProbasLeafs() throws CalculInterrompu {
        try (ExecutorService executorService = Executors.newFixedThreadPool(N_PROCESSEURS)) {
            
            for (ComboDenombrable comboDenombrable : leafs) {
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
        catch (Exception ignored) {
        }

        if (Estimateur.estInterrompu()) throw new CalculInterrompu();
    }

    
    private void initialiserClusters(List<ClusterEquilibrage> clusters) throws CalculInterrompu {
        try (ExecutorService executorService = Executors.newFixedThreadPool(N_PROCESSEURS)) {

            for (ClusterEquilibrage cluster : clusters) {
                ProbaObservations probaObservations = new ProbaObservations(cluster);
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
        catch (Exception ignored) {
        }

        if (Estimateur.estInterrompu()) throw new CalculInterrompu();

        for (ClusterEquilibrage cluster : clusters) {
            cluster.initialiserStrategie(pas);
            cluster.setStrategiePlusProbable();
        }
    }

    
    private void equilibrerCombosDansClusters(List<ClusterEquilibrage> clusters) throws CalculInterrompu {
        for (ClusterEquilibrage clusterEquilibrage : clusters) {
            List<ComboDansCluster> combosDansClusters = clusterEquilibrage.getCombos();
            float[] pActionsCluster = clusterEquilibrage.getStrategieActuelle();

            for (ComboDansCluster combo : combosDansClusters) {
                combo.initialiserStrategie(pas);
                combo.setStrategieMediane();
            }

            equilibrer(combosDansClusters, pActionsCluster);

            
            for (ComboDansCluster combo : combosDansClusters) {
                combo.fixerStrategie();
            }
        }
    }

    
    private void equilibrer(List<? extends NoeudEquilibrage> noeuds, float[] pActionsReelles) throws CalculInterrompu {
        Equilibrateur equilibrateur = new Equilibrateur(noeuds, pActionsReelles);
        equilibrateur.lancerEquilibrage();
    }

    
    private float verifierFold() {
        float pFoldReel;
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

    
    public List<ComboDenombrable> getCombosEquilibres() {
        return leafs;
    }

}
