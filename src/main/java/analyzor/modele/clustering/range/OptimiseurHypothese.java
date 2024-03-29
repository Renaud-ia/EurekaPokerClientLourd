package analyzor.modele.clustering.range;

import analyzor.modele.clustering.cluster.ClusterDeBase;
import analyzor.modele.clustering.cluster.ClusterRange;
import analyzor.modele.clustering.objets.ComboPostClustering;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.ProbaObservations;
import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.estimation.CalculInterrompu;
import analyzor.modele.estimation.Estimateur;
import analyzor.modele.utils.Bits;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class OptimiseurHypothese {
    private static final int MAX_CLASSES_PAR_COMPOSANTE = 4;
    private static final int MIN_CLUSTERS_PAR_RANGE = 3;
    private static final int MAX_CLUSTERS_PAR_RANGE = 7;
    private static final float N_SERVIS_MINIMAL = 50;

    private static final float N_SERVIS_OPTIMAL = 500;
    private static final int FACTEUR_REDUCTION_PAS = 10;
    private final List<HypotheseClustering> hypotheses;
    private final int nObservations;

    OptimiseurHypothese(int nSituations) {
        HypotheseClustering.setNombreObservations(nSituations);
        this.nObservations = nSituations;
        this.hypotheses = new ArrayList<>();
    }



    void creerHypotheses(List<ComboPreClustering> combosInitiaux) throws CalculInterrompu {
        if (Estimateur.estInterrompu()) throw new CalculInterrompu();


        int nComposantes = combosInitiaux.getFirst().nDimensions();
        HypotheseClustering.ajouterCombos(combosInitiaux, nComposantes);


        List<List<Integer>> combinaisons = new ArrayList<>();
        genererCombinaisons(nComposantes, MAX_CLASSES_PAR_COMPOSANTE, 1, new ArrayList<>(), combinaisons);


        for (List<Integer> valeursHypothese : combinaisons) {
            int[] hypothese = new int[valeursHypothese.size()];

            int produitCombinaisons = 1;
            for (int i = 0; i < valeursHypothese.size(); i++) {
                hypothese[i] = valeursHypothese.get(i);
                produitCombinaisons *= valeursHypothese.get(i);
            }

            if (produitCombinaisons > MAX_CLUSTERS_PAR_RANGE || produitCombinaisons < MIN_CLUSTERS_PAR_RANGE) continue;

            HypotheseClustering nouvelleHypothese = new HypotheseClustering(hypothese);
            this.hypotheses.add(nouvelleHypothese);
        }

    }

    
    List<ComboPostClustering> meilleureHypothese() throws CalculInterrompu {

        float pas = 1f;
        int tour = 0;
        while(tour ++ < 2) {

            pas = actualiserPas(pas);


            ajusterHypotheses();

        }

        return selectionnerMeilleureHypothese();
    }



    
    private List<ComboPostClustering> selectionnerMeilleureHypothese() {
        float coutPlusEleve = Float.MIN_VALUE;
        HypotheseClustering meilleureHypothese = null;

        for (HypotheseClustering hypotheseClustering : hypotheses) {
            float coutHypothese = qualiteHypothese(hypotheseClustering);
            if (coutHypothese > coutPlusEleve) {
                coutPlusEleve = coutHypothese;
                meilleureHypothese = hypotheseClustering;
            }

        }

        if (meilleureHypothese == null) throw new RuntimeException("Aucune meilleure hypothèse trouvée");

        return isolerCentres(meilleureHypothese.clusteringActuel());
    }

    
    private List<ComboPostClustering> isolerCentres(List<ClusterRange> clusterRanges) {
        List<ComboPostClustering> centresGravite = new ArrayList<>();

        for (ClusterRange cluster : clusterRanges) {
            centresGravite.add(new ComboPostClustering(cluster.getCentreGravite().getNoeudEquilibrage()));
        }

        return centresGravite;
    }

    
    private float qualiteHypothese(HypotheseClustering hypotheseClustering) {


        List<ClusterRange> clustersHypothese = hypotheseClustering.clusteringActuel();
        int poidsNombreClusters = (clustersHypothese.size() * 10);
        float poidsInterCluster = moyenneDistanceInterCluster(clustersHypothese);
        float penaliteMinServis = penaliteMinServis(clustersHypothese);

        return (poidsNombreClusters + poidsInterCluster) * penaliteMinServis;
    }

    
    private float moyenneDistanceInterCluster(List<ClusterRange> clustersFormes) {



        float totaleDistance = 0;
        int compte = 0;

        for (int i = 0; i < clustersFormes.size(); i++) {
            for (int j = i + 1; j < clustersFormes.size(); j++) {
                ClusterRange cluster1 = clustersFormes.get(i);
                ClusterRange cluster2 = clustersFormes.get(j);


                if (cluster1.getCentreGravite() == null || cluster2.getCentreGravite() == null) return -Float.MAX_VALUE;

                float distance =
                        cluster1.getCentreGravite().getEquiteFuture().distance(cluster2.getCentreGravite().getEquiteFuture());
                totaleDistance += distance;
                compte++;
            }
        }
        return totaleDistance / compte;
    }

    
    private float plusGrandeDistanceInterCluster(HypotheseClustering hypotheseClustering) {



        float plusGrandeDistance = 0;

        List<ClusterRange> clustersFormes = hypotheseClustering.clusteringActuel();

        for (int i = 0; i < clustersFormes.size(); i++) {
            for (int j = i + 1; j < clustersFormes.size(); j++) {
                ClusterRange cluster1 = clustersFormes.get(i);
                ClusterRange cluster2 = clustersFormes.get(j);


                if (cluster1.getCentreGravite() == null || cluster2.getCentreGravite() == null) return Float.MIN_VALUE;

                float distance =
                        cluster1.getCentreGravite().getEquiteFuture().distance(cluster2.getCentreGravite().getEquiteFuture());
                if (distance > plusGrandeDistance) {
                    plusGrandeDistance = distance;
                }
            }
        }
        return plusGrandeDistance;
    }

    
    private float penaliteMinServis(List<ClusterRange> clustersFormes) {
        int minServis = Integer.MAX_VALUE;


        for (ClusterDeBase<ComboPreClustering> cluster : clustersFormes) {
            float nCombosServis = 0f;
            for (ComboPreClustering combo : cluster.getObjets()) {
                nCombosServis += combo.getPCombo() * nObservations;
            }

            if ((int) nCombosServis < minServis) {
                minServis = (int) nCombosServis;
            }
        }

        InverseSigmoidFunction inverseSigmoidFunction = new InverseSigmoidFunction(N_SERVIS_MINIMAL, N_SERVIS_OPTIMAL);
        return (float) inverseSigmoidFunction.getValeur(minServis);
    }

    private void ajusterHypotheses() throws CalculInterrompu {
        int nAjustements = FACTEUR_REDUCTION_PAS - 1;
        HypotheseClustering.setNombreAjustements(nAjustements);

        final int N_PROCESSEURS = Runtime.getRuntime().availableProcessors() - 3;

        try (ExecutorService executorService = Executors.newFixedThreadPool(N_PROCESSEURS)) {

            for (HypotheseClustering hypotheseClustering : hypotheses) {
                executorService.submit(hypotheseClustering);
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

    private void supprimerMoinsBonneHypothese() {
        float coutPlusEleve = Float.MIN_VALUE;
        HypotheseClustering pireHypothese = null;

        for (HypotheseClustering hypotheseClustering : hypotheses) {
            float coutHypothese = hypotheseClustering.coutAjustementActuel();
            if (coutHypothese > coutPlusEleve) {
                coutPlusEleve = coutHypothese;
                pireHypothese = hypotheseClustering;
            }
        }

        if (pireHypothese == null) throw new RuntimeException("Aucune pire hypothèse trouvée");
        hypotheses.remove(pireHypothese);
    }


    private float actualiserPas(float pas) {
        float nouveauPas = pas / FACTEUR_REDUCTION_PAS;
        HypotheseClustering.setPas(nouveauPas);
        return nouveauPas;
    }

    
    private static void genererCombinaisons(
            int nComposantes,
            int MAX_VALEUR,
            int currentVariable,
            List<Integer> currentCombination,
            List<List<Integer>> combinations) {


        if (currentVariable > nComposantes) {
            combinations.add(new ArrayList<>(currentCombination));
            return;
        }


        for (int value = 1; value <= MAX_VALEUR; value++) {

            currentCombination.add(value);

            genererCombinaisons(nComposantes, MAX_VALEUR,
                    currentVariable + 1, currentCombination, combinations);

            currentCombination.removeLast();
        }
    }

    
    private static class InverseSigmoidFunction {
        private final static float yMin = 0.1f;
        private final static float yMax = 0.9f;
        private final static int alpha = 1;
        private final static int VALEUR_PLATEAU = 1;
        private final double coeffA;
        private final double coeffB;

        public InverseSigmoidFunction(double xMin, double xMax) {
            this.coeffA = (2 + 2) / (xMax - xMin);
            this.coeffB = -2 - (coeffA * xMin);
        }

        public double getValeur(double x) {
            double valeurMappee = coeffA * x + coeffB;
            double valeurY = VALEUR_PLATEAU / (1 + Math.exp(-alpha * valeurMappee));
            return Math.min(Math.max(yMin, valeurY), yMax);
        }
    }
}