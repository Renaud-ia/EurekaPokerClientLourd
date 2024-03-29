package analyzor.modele.clustering.range;

import analyzor.modele.clustering.cluster.ClusterDeBase;
import analyzor.modele.clustering.cluster.ClusterRange;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.denombrement.CalculEquitePreflop;
import analyzor.modele.denombrement.combos.DenombrableIso;
import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.evaluation.MatriceEquite;
import analyzor.modele.utils.ManipulationTableaux;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


class HypotheseClustering implements Runnable {
    
    
    
    
    
    private static final float POIDS_N_SERVIS = 0f;
    
    private static final float N_SERVIS_MINIMAL = 50;
    
    private static final float N_SERVIS_OPTIMAL = 500;
    private static List<ComboPreClustering> combos;
    
    private static float[] centreGlobalNuage;
    private static float[] minValeurs;
    private static float[] maxValeurs;
    private static int nObservations;
    private static float[] pasActuel;
    private static int nAjustements;

    
    private final InverseSigmoidFunction coutCombosServis;
    
    private float[][] valeursAjustementsCourantes;
    private List<ClusterRange> meilleurClustering;
    private float coutActuel;

    
    HypotheseClustering(int[] hypotheses) {
        coutCombosServis = new InverseSigmoidFunction(N_SERVIS_MINIMAL, N_SERVIS_OPTIMAL);
        initialiserValeurs(hypotheses);
        meilleurClustering = new ArrayList<>();
    }

    

    

    
    static void ajouterCombos(List<ComboPreClustering> combosInitiaux, int nComposantes) {
        combos = combosInitiaux;
        
        minValeurs = new float[nComposantes];
        Arrays.fill(minValeurs, Float.MAX_VALUE);
        maxValeurs = new float[nComposantes];
        Arrays.fill(maxValeurs, Float.MIN_VALUE);

        centreGlobalNuage = new float[combosInitiaux.getFirst().getEquiteFuture().nDimensions()];
        Arrays.fill(maxValeurs, 0);

        for (ComboPreClustering combo : combosInitiaux) {
            for (int i = 0; i < combo.valeursNormalisees().length; i++) {
                minValeurs[i] = Math.min(minValeurs[i], combo.valeursNormalisees()[i]);
                maxValeurs[i] = Math.max(maxValeurs[i], combo.valeursNormalisees()[i]);
            }

            for (int j = 0; j < combo.getEquiteFuture().aPlat().length; j++) {
                centreGlobalNuage[j] += combo.getEquiteFuture().aPlat()[j] / combosInitiaux.size();
            }
        }
    }

    static void setNombreObservations(int nombreObservations) {
        nObservations = nombreObservations;
    }

    
    static void setPas(float pctPlageValeur) {
        pasActuel = new float[minValeurs.length];
        for (int i =0; i < minValeurs.length; i++) {
            pasActuel[i] = (maxValeurs[i] - minValeurs[i]) * pctPlageValeur;
        }
    }

    static void setNombreAjustements(int nombreAjustements) {
        nAjustements = nombreAjustements;
    }

    

    
    @Override
    public void run() {
        for (int i = 0; i < nAjustements; i++) {
            
            float[][][] hypothesesParAxe = toutesLesHypotheses();

            
            int[] hypotheseActuelle = new int[hypothesesParAxe.length];
            Arrays.fill(hypotheseActuelle, 0);
            hypotheseActuelle[0] = -1;

            float[][] hypotheseBornes;
            boolean hypotheseChangee = false;
            while ((hypotheseBornes = prochaineHypothese(hypothesesParAxe, hypotheseActuelle)) != null) {

                
                float coutHypothese = calculerCout(hypotheseBornes);

                if (coutHypothese < coutActuel) {
                    hypotheseChangee = true;
                    coutActuel = coutHypothese;
                    
                    valeursAjustementsCourantes = ManipulationTableaux.copierTableau(hypotheseBornes);

                }
            }

            
            if (!hypotheseChangee) break;
        }
    }

    
    private float[][][] toutesLesHypotheses() {
        final int nBornesInchangees = 2;
        float[][][] toutesLesHypotheses = new float[valeursAjustementsCourantes.length][][];

        for (int iComposante = 0; iComposante < valeursAjustementsCourantes.length; iComposante++) {
            int nBornesModifiables = valeursAjustementsCourantes[iComposante].length - nBornesInchangees;
            if (nBornesModifiables == 0) {
                toutesLesHypotheses[iComposante] = new float[1][2];
                toutesLesHypotheses[iComposante][0][0] = minValeurs[iComposante];
                toutesLesHypotheses[iComposante][0][1] = maxValeurs[iComposante];

                continue;
            }

            int[][] hypothesesChangementPas = matriceChangementPas(nBornesModifiables);
            toutesLesHypotheses[iComposante] = new float[hypothesesChangementPas.length][];

            for (int jChangement = 0; jChangement < hypothesesChangementPas.length; jChangement++) {
                int[] changementPas = hypothesesChangementPas[jChangement];
                float[] nouvellesBornes = new float[changementPas.length + nBornesInchangees];

                
                nouvellesBornes[0] = minValeurs[iComposante];
                nouvellesBornes[nouvellesBornes.length - 1] = maxValeurs[iComposante];

                for (int kHypothese = 0; kHypothese < changementPas.length; kHypothese++) {
                    
                    int indexBorne = kHypothese + 1;
                    float increment = changementPas[kHypothese] * pasActuel[iComposante];
                    nouvellesBornes[indexBorne] = valeursAjustementsCourantes[iComposante][indexBorne] + increment;
                }
                toutesLesHypotheses[iComposante][jChangement] = nouvellesBornes;

            }
        }

        return toutesLesHypotheses;
    }

    
    private int[][] matriceChangementPas(int nBornesModifiables) {
        int nPossilites = 3;
        
        int tailleMatrice = ((int) Math.pow(nPossilites, nBornesModifiables)) - 1;

        int[][] matriceChangement = new int[tailleMatrice][nBornesModifiables];
        Arrays.fill(matriceChangement[0], -1);

        for (int indexMatrice = 1; indexMatrice < matriceChangement.length; indexMatrice++) {
            
            int[] changements = matriceChangement[indexMatrice - 1].clone();
            int indexChangement = nBornesModifiables - 1;

            
            while(true) {
                
                if (changements[indexChangement] < 1) {
                    changements[indexChangement]++;

                    boolean aucunChangement = true;
                    
                    for (int changement : changements) {
                        if (changement != 0) {
                            matriceChangement[indexMatrice] = changements;
                            aucunChangement = false;
                            break;
                        }
                    }
                    if (!aucunChangement) break;
                }
                else {
                    
                    
                    changements[indexChangement] = -1;
                    indexChangement--;
                    if (indexChangement < 0) break;
                }
            }
        }

        return matriceChangement;
    }

    
    private float[][] prochaineHypothese(float[][][] hypothesesParAxe, int[] hypotheseActuelle) {
        for (int iComposante = 0; iComposante < hypothesesParAxe.length; iComposante++) {
            int prochaineHypothese = hypotheseActuelle[iComposante] + 1;

            
            if (prochaineHypothese < hypothesesParAxe[iComposante].length) {
                hypotheseActuelle[iComposante]++;
                break;
            }
            
            else if (iComposante == hypothesesParAxe.length -1) return null;

            
            else {
                hypotheseActuelle[iComposante] = 0;
            }

        }

        
        float[][] prochaineHypothese = new float[hypothesesParAxe.length][];
        for (int i = 0; i < hypothesesParAxe.length; i++) {
            prochaineHypothese[i] = hypothesesParAxe[i][hypotheseActuelle[i]];
        }

        return prochaineHypothese;
    }

    
    float coutAjustementActuel() {
        return coutActuel;
    }

    
    List<ClusterRange> clusteringActuel() {
        List<ClusterRange> clustersFinaux = trouverClusters(valeursAjustementsCourantes);

        for (ClusterRange clusterRange : clustersFinaux) {
            
            qualiteCluster(clusterRange);
        }

        return clustersFinaux;
    }


    

    
    private void initialiserValeurs(int[] hypotheses) {
        if (minValeurs == null || maxValeurs == null)
            throw new RuntimeException("Valeurs min et max pas initialisées");

        initialisationPercentiles(hypotheses);
        
        coutActuel = calculerCout(valeursAjustementsCourantes);
    }

    
    private void initialisationPercentiles(int[] hypotheses) {
        valeursAjustementsCourantes = new float[hypotheses.length][];
        for (int iComposante = 0; iComposante < hypotheses.length; iComposante++) {
            final int index = iComposante;
            combos.sort(Comparator.comparing(objet -> objet.valeursNormalisees()[index]));

            int nBornes = hypotheses[iComposante] + 1;
            valeursAjustementsCourantes[iComposante] = new float[nBornes];

            int nPointsParCluster = combos.size() / hypotheses[iComposante];
            int nPointsParcourus = 0;

            for (int jBorne = 1; jBorne < nBornes - 1; jBorne++) {
                while (nPointsParcourus++ < combos.size()) {
                    if (nPointsParcourus + 1 >= (jBorne * nPointsParCluster)) {
                        valeursAjustementsCourantes[iComposante][jBorne] =
                                combos.get(nPointsParcourus).valeursNormalisees()[iComposante];
                        break;
                    }
                }
            }

            valeursAjustementsCourantes[iComposante][0] = minValeurs[iComposante];
            valeursAjustementsCourantes[iComposante][nBornes - 1] = maxValeurs[iComposante];
        }
    }

    
    @Deprecated
    private void initialisationAmplitude(int[] hypotheses) {
        valeursAjustementsCourantes = new float[hypotheses.length][];
        for (int i = 0; i < hypotheses.length; i++) {
            
            int nBornes = hypotheses[i] + 1;
            valeursAjustementsCourantes[i] = new float[nBornes];

            for (int j = 0; j < nBornes; j++) {
                float valeurInitiale = (j * ((maxValeurs[i] - minValeurs[i]) / hypotheses[i])) + minValeurs[i];
                valeursAjustementsCourantes[i][j] = valeurInitiale;
            }
        }

    }

    
    private float calculerCout(float[][] hypotheseBornes) {
        final int minEffectif = 2;
        
        List<ClusterRange> clustersHypothese = trouverClusters(hypotheseBornes);

        HashMap<ClusterRange, Float> homogeneites = new HashMap<>();

        float coutTotal = 0f;
        for (ClusterRange cluster : clustersHypothese) {
            
            if (cluster.getEffectif() < minEffectif) {
                return Float.MAX_VALUE;
            }

            
            float qualiteCluster = qualiteCluster(cluster);

            coutTotal += qualiteCluster;
        }

        return coutTotal / clustersHypothese.size();
    }

    
    private float qualiteCluster(ClusterRange cluster) {
        float pctClusterTeste = 0.5f;
        int minCombosTestes = Math.max(combos.size() / 6, 3);
        int nCombosTestes = (int) Math.max(minCombosTestes, cluster.getEffectif() * pctClusterTeste);

        float meilleureQualite = Float.MAX_VALUE;
        ComboPreClustering centreGravite = null;

        for (ComboPreClustering comboRange : cluster.getObjets()) {
            float qualiteCentre = qualiteCentreGravite(comboRange, cluster, nCombosTestes);
            if (qualiteCentre < meilleureQualite) {
                meilleureQualite = qualiteCentre;
                centreGravite = comboRange;
            }
        }

        cluster.setCentreGravite(centreGravite);

        if (centreGravite == null) return 1;

        return meilleureQualite;
    }

    
    private float qualiteCentreGravite(ComboPreClustering comboRange, ClusterRange cluster, int nCombosTestes) {
        

        
        List<ComboPreClustering> combosPlusProches = new ArrayList<>();
        for (int i = 0; i < nCombosTestes; i++) {
            float plusFaibleDistance = Float.MAX_VALUE;
            ComboPreClustering comboPlusProche = null;

            ComboIso combo1 = ((DenombrableIso) comboRange.getNoeudEquilibrage().getComboDenombrable()).getCombo();
            
            if (CalculEquitePreflop.ppDistanceSpeciale.contains(combo1)) return 1;

            for (ComboPreClustering autreCombo : combos) {
                if (autreCombo == comboRange) continue;
                if (combosPlusProches.contains(autreCombo)) continue;
                ComboIso combo2 = ((DenombrableIso) autreCombo.getNoeudEquilibrage().getComboDenombrable()).getCombo();

                float distanceEquite =
                        CalculEquitePreflop.getInstance().distanceCombos(combo1, combo2);

                if (distanceEquite < plusFaibleDistance) {
                    plusFaibleDistance = distanceEquite;
                    comboPlusProche = autreCombo;
                }
            }
            combosPlusProches.add(comboPlusProche);
        }

        int nErreurs = 0;
        for (ComboPreClustering comboVoisin : combosPlusProches) {
            if (!(cluster.getObjets().contains(comboVoisin))) nErreurs++;
        }

        return (float) nErreurs / nCombosTestes;
    }

    
    private float moyenneVarianceIntraCluster(List<ClusterRange> clustersHypothese,
                                              HashMap<ClusterRange, Float> homogeneites) {
        final float MINIMUM_COMBOS_PAR_CLUSTER = 2;

        float varianceIntraCluster = 0f;
        float plusGrandeDistance = 0f;

        for (ClusterRange clusterRange :clustersHypothese) {
            if (clusterRange.getEffectif() < MINIMUM_COMBOS_PAR_CLUSTER) {
                return Float.MAX_VALUE;
            }
            varianceIntraCluster += recupererHomogeneite(clusterRange, homogeneites) / clusterRange.getEffectif();

            for (ClusterRange autreCluster : clustersHypothese) {
                if (clusterRange == autreCluster) continue;
                float distance = clusterRange.distance(autreCluster);
                plusGrandeDistance = Math.max(distance, plusGrandeDistance);
            }
        }

        return (varianceIntraCluster / clustersHypothese.size()) / plusGrandeDistance;
    }


    
    private float coutNombreServis(ClusterRange cluster) {
        float nCombosServis = 0f;
        for (ComboPreClustering combo : cluster.getObjets()) {
            nCombosServis += combo.getPCombo() * nObservations;
        }

        return (float) coutCombosServis.getValeur(nCombosServis);
    }

    
    private float indiceDaviesBouldin(ClusterRange cluster,
                                      List<ClusterRange> clustersHypothese,
                                      HashMap<ClusterRange, Float> homogeneites) {

        float homogeneite = recupererHomogeneite(cluster, homogeneites);

        float maxK = Float.MIN_VALUE;
        for (ClusterRange autreCluster : clustersHypothese) {
            if (cluster == autreCluster) continue;
            if (autreCluster.getEffectif() == 0) continue;
            float homogeneiteAutreCluster = recupererHomogeneite(autreCluster, homogeneites);
            float distanceCentroides = cluster.distance(autreCluster);

            float valeurK = (homogeneiteAutreCluster + homogeneite) / distanceCentroides;
            maxK = Math.max(valeurK, maxK);
        }

        return maxK;
    }

    
    private float indiceCalinskiHarabasz(List<ClusterRange> clustersHypothese,
                                         HashMap<ClusterRange, Float> homogeneites) {
        final float MINIMUM_COMBOS_PAR_CLUSTER = 2;

        float varianceInterCluster = 0f;
        float varianceIntraCluster = 0f;
        for (ClusterRange clusterRange :clustersHypothese) {
            if (clusterRange.getEffectif() < MINIMUM_COMBOS_PAR_CLUSTER) {
                return Float.MIN_VALUE;
            }
            varianceInterCluster += clusterRange.distance(centreGlobalNuage) / clustersHypothese.size();
            varianceIntraCluster += recupererHomogeneite(clusterRange, homogeneites);
        }

        return (varianceInterCluster / varianceIntraCluster) * ((float) combos.size() - clustersHypothese.size()
                / ((float) clustersHypothese.size() -1));
    }

    
    private float recupererHomogeneite(ClusterRange cluster,
                                       HashMap<ClusterRange, Float> homogeneites) {
        Float valeurStockee = homogeneites.get(cluster);
        if (valeurStockee == null) {
            valeurStockee = cluster.homogeneite();
            homogeneites.put(cluster, valeurStockee);
        }

        return valeurStockee;
    }

    
    private float fonctionCout(float qualiteCluster, float coutNombreServis) {
        return (float) (qualiteCluster * Math.pow(coutNombreServis, POIDS_N_SERVIS));
    }

    
    private List<ClusterRange> trouverClusters(float[][] hypotheseBornes) {
        List<ClusterRange> clustersSepares = null;

        
        for (int indexComposante = 0; indexComposante < hypotheseBornes.length; indexComposante++) {
            float[] bornesHypothese = hypotheseBornes[indexComposante];

            clustersSepares = separerClusters(
                    clustersSepares,
                    indexComposante,
                    bornesHypothese

            );
        }

        return clustersSepares;
    }

    
    private List<ClusterRange> separerClusters(
            List<ClusterRange> clustersOrigine,
            int indexComposante,
            float[] bornesHypothese) {
        
        if (clustersOrigine == null) {
            clustersOrigine = new ArrayList<>();
            clustersOrigine.add(new ClusterRange(combos));
        }

        List<ClusterRange> clustersFinaux = new ArrayList<>();

        for (ClusterRange clusterOriginal : clustersOrigine) {
            
            HashMap<Integer, ClusterRange> mapClusters = new HashMap<>();
            for (int i = 0; i <= (bornesHypothese.length - 2); i++) {
                mapClusters.put(i, new ClusterRange());
            }

            
            for (ComboPreClustering comboCluster : clusterOriginal.getObjets()) {
                Integer indexMap = null;
                float valeurStockee = comboCluster.valeursNormalisees()[indexComposante];

                
                if (valeurStockee <= bornesHypothese[1]) {
                    indexMap = 0;
                }
                else if (valeurStockee >= bornesHypothese[bornesHypothese.length - 2]) {
                    indexMap = bornesHypothese.length - 2;
                }

                
                else {
                    for (int j = 0; j < bornesHypothese.length - 1; j++) {
                        if (valeurStockee > bornesHypothese[j] && valeurStockee <= bornesHypothese[j + 1]) {
                            indexMap = j;
                            break;
                        }
                    }
                }

                if (indexMap == null) throw new RuntimeException("Aucune index trouvé");

                mapClusters.get(indexMap).ajouterObjet(comboCluster);
            }

            clustersFinaux.addAll(mapClusters.values());
        }

        return clustersFinaux;
    }

    public int nClusters() {
        return clusteringActuel().size();
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
            double valeurY = VALEUR_PLATEAU / (1 + Math.exp(alpha * valeurMappee));
            return Math.min(Math.max(yMin, valeurY), yMax);
        }
    }

}
