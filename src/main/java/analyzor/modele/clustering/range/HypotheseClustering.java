package analyzor.modele.clustering.range;

import analyzor.modele.clustering.cluster.ClusterDeBase;
import analyzor.modele.clustering.cluster.ClusterRange;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.utils.ManipulationTableaux;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * hypothèse sur le clustering de range
 * une hypothèse est simplement un nombre de divisions des composantes de l'ACP
 * calcule une erreur lié à une fonction objectif
 */
class HypotheseClustering {
    private final static Logger logger = LogManager.getLogger(HypotheseClustering.class);
    // todo tester des valeurs
    // poids donné aux nombres de combos servis de chaque cluster (plus il est proche de 0, moins ça va peser)
    // plages de valeurs entre [0;+infini[
    private static final float POIDS_N_SERVIS = 0.5f;
    // nombre de combos servis qu'on veut par cluster, plus on se rapproche de cette valeur, plus le cluster sera pénalisé
    private static final float N_SERVIS_MINIMAL = 50;
    // valeur optimale qu'on veut => au dessus ne change plus rien
    private static final float N_SERVIS_OPTIMAL = 500;
    private static List<ComboPreClustering> combos;
    private static float[] minValeurs;
    private static float[] maxValeurs;
    private static int nObservations;
    private static float[] pasActuel;
    private static int nAjustements;

    // attributs privés
    private final InverseSigmoidFunction coutCombosServis;
    // contient les bornes de l'ajustement actuel
    private float[][] valeursAjustementsCourantes;
    private float coutActuel;

    /**
     * @param hypotheses spécifie le nombre de classes pour chaque composante
     */
    HypotheseClustering(int[] hypotheses) {
        coutCombosServis = new InverseSigmoidFunction(N_SERVIS_MINIMAL, N_SERVIS_OPTIMAL);
        initialiserValeurs(hypotheses);
    }

    // interface publique permettant à l'optimiseur de faire tourner les hypothèses

    // méthodes statiques

    /**
     * méthode statique pour initialiser les références vers les objets qu'on cherche à optimiser
     * on va initialiser les plages de valeurs
     */
    static void ajouterCombos(List<ComboPreClustering> combosInitiaux, int nComposantes) {
        combos = combosInitiaux;
        // on détermine les valeurs min et max de l'ensemble
        minValeurs = new float[nComposantes];
        Arrays.fill(minValeurs, Float.MAX_VALUE);
        maxValeurs = new float[nComposantes];
        Arrays.fill(maxValeurs, Float.MIN_VALUE);

        for (ComboPreClustering combo : combosInitiaux) {
            for (int i = 0; i < combo.valeursNormalisees().length; i++) {
                minValeurs[i] = Math.min(minValeurs[i], combo.valeursNormalisees()[i]);
                maxValeurs[i] = Math.max(maxValeurs[i], combo.valeursNormalisees()[i]);
            }
        }
    }

    static void setNombreObservations(int nombreObservations) {
        nObservations = nombreObservations;
    }

    /**
     * définit un pas d'ajustement des valeurs
     * @param pctPlageValeur exprimé en % de la plage de valeurs totales
     */
    static void setPas(float pctPlageValeur) {
        pasActuel = new float[minValeurs.length];
        for (int i =0; i < minValeurs.length; i++) {
            pasActuel[i] = (maxValeurs[i] - minValeurs[i]) * pctPlageValeur;
        }
    }

    static void setNombreAjustements(int nombreAjustements) {
        nAjustements = nombreAjustements;
    }

    // méthodes dynamiques

    /**
     * ajuste une fois les valeurs selon le pas défini
     * important ne pas modifier le pas
     * todo OPTIMISATION: on pourrait multiprocesser (= méthode run)
     */
    void ajusterValeurs() {
        for (int i = 0; i < nAjustements; i++) {
            // on crée les couples de valeurs possibles (combinaisons de augmentation, diminution ou pas de changement)
            float[][][] hypothesesParAxe = toutesLesHypotheses();

            // initialisation du tableau avec première valeur à -1
            int[] hypotheseActuelle = new int[hypothesesParAxe.length];
            Arrays.fill(hypotheseActuelle, 0);
            hypotheseActuelle[0] = -1;

            float[][] hypotheseBornes;
            boolean hypotheseChangee = false;
            while ((hypotheseBornes = prochaineHypothese(hypothesesParAxe, hypotheseActuelle)) != null) {
                // on calcule le cout de chaque ajustement
                float coutHypothese = calculerCout(hypotheseBornes);

                logger.trace("Hypothèse testée : " + Arrays.deepToString(hypotheseBornes));
                logger.trace("Cout : " + coutHypothese);

                if (coutHypothese < coutHypothese) {
                    hypotheseChangee = true;
                    coutActuel = coutHypothese;
                    // important on fait une copie profonde car l'hypothèse est affectée à une autre valeur ensuite
                    valeursAjustementsCourantes = ManipulationTableaux.copierTableau(hypotheseBornes);

                    logger.trace("Meilleure hypothèse modifiée : " + Arrays.deepToString(valeursAjustementsCourantes));
                }
            }

            // si on a pas changé d'hypothèse, rien ne sert de continuer car le pas n'a pas changé
            if (!hypotheseChangee) break;
        }
    }

    /**
     * renvoie toutes les hypothèses par axe
     * se base sur la valeur de l'ajustement actuel et le pas fixé
     * float[] => liste des axes
     * float[][] => liste des hypothèses possibles pour l'axe
     * float[][][] => liste des bornes pour l'hypothèse (avec 0 et max inclus mais qui ne peuvent changer)
     * @return toutes les hypothèses de valeurs de bornes pour chaque axe
     */
    private float[][][] toutesLesHypotheses() {
        final int nBornesInchangees = 2;
        float[][][] toutesLesHypotheses = new float[valeursAjustementsCourantes.length][][];

        for (int iComposante = 0; iComposante < valeursAjustementsCourantes.length; iComposante++) {
            int nBornesModifiables = valeursAjustementsCourantes[iComposante].length - nBornesInchangees;
            if (nBornesModifiables == 0) {
                toutesLesHypotheses[iComposante] = new float[1][];
                toutesLesHypotheses[iComposante][0] = valeursAjustementsCourantes[iComposante];
                continue;
            }

            int[][] hypothesesChangementPas = matriceChangementPas(nBornesModifiables);
            toutesLesHypotheses[iComposante] = new float[hypothesesChangementPas.length][];

            for (int jChangement = 0; jChangement < hypothesesChangementPas.length; jChangement++) {
                int[] changementPas = hypothesesChangementPas[jChangement];
                float[] nouvellesBornes = new float[changementPas.length + nBornesInchangees];

                // important la borne min et max ne change jamais
                nouvellesBornes[0] = minValeurs[iComposante];
                nouvellesBornes[nouvellesBornes.length - 1] = maxValeurs[iComposante];

                for (int kHypothese = 0; kHypothese < changementPas.length; kHypothese++) {
                    // on décale d'un index car le premier c'est la valeur min
                    int indexBorne = kHypothese + 1;
                    float increment = changementPas[kHypothese] * pasActuel[iComposante];
                    nouvellesBornes[indexBorne] = valeursAjustementsCourantes[iComposante][indexBorne] + increment;
                }
                toutesLesHypotheses[iComposante][jChangement] = nouvellesBornes;
            }
        }

        return toutesLesHypotheses;
    }

    /**
     * renvoie une matrice euclidienne des combinaisons possibles
     * [-1, -1], [-1, 0], [-1 1] etc etc
     * ne renvoie jamais [0;0] car veut dire pas de changement
     * @param nBornesModifiables nombre de bornes qu'on peut modifier
     * @return renvoie une matrice euclidienne des combinaisons possibles
     */
    private int[][] matriceChangementPas(int nBornesModifiables) {
        int nPossilites = 3;
        // on retire 1 car on ne prend pas en compte le cas où aucun changement
        int tailleMatrice = ((int) Math.pow(nPossilites, nBornesModifiables)) - 1;

        int[][] matriceChangement = new int[tailleMatrice][nBornesModifiables];
        Arrays.fill(matriceChangement[0], -1);

        for (int indexMatrice = 1; indexMatrice < matriceChangement.length; indexMatrice++) {
            // on récupère les changements de la ligne précédente
            int[] changements = matriceChangement[indexMatrice - 1].clone();
            int indexChangement = nBornesModifiables - 1;

            // on trouve l'index à changer de manière récursive
            while(true) {
                // si on peut changer l'index concerné,
                if (changements[indexChangement] < 1) {
                    changements[indexChangement]++;

                    boolean aucunChangement = true;
                    // on vérifie que c'est pas 0, 0, 0, ...
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
                    // si le changement est déjà au max
                    // on le remet à -1 et on passe au précédent
                    changements[indexChangement] = -1;
                    indexChangement--;
                    if (indexChangement < 0) break;
                }
            }
        }

        return matriceChangement;
    }

    /**
     * renvoie la prochaine hypothèses de bornes
     * en construisant une matrice euclidienne des hypothèses de bornes pour chaque axe
     * @param hypothesesParAxe matrice des toutes les hypothèses
     * @param hypotheseActuelle sert à garder en mémoire l'espace des hypothèses explorés
     *                          important doit être initialisé à [-1; 0; 0, etc] pour que ça fonctionne
     * @return les bornes pour l'hypothèse suivante, null si plus d'hypothèse
     */
    private float[][] prochaineHypothese(float[][][] hypothesesParAxe, int[] hypotheseActuelle) {
        for (int iComposante = 0; iComposante < hypothesesParAxe.length; iComposante++) {
            int prochaineHypothese = hypotheseActuelle[iComposante] + 1;

            // si l'hypothèse suivante existe
            if (prochaineHypothese < hypothesesParAxe[iComposante].length) {
                hypotheseActuelle[iComposante]++;
                break;
            }
            // si on a tout parcouru on retourne null
            else if (iComposante == hypothesesParAxe.length -1) return null;

            // sinon on réinitialise à zéro cette composante et on passe à l'hypothèse suivante
            else {
                hypotheseActuelle[iComposante] = 0;
            }

        }

        // on retourne l'hypothèse correspondante aux bornes
        float[][] prochaineHypothese = new float[hypothesesParAxe.length][];
        for (int i = 0; i < hypothesesParAxe.length; i++) {
            prochaineHypothese[i] = hypothesesParAxe[i][hypotheseActuelle[i]];
        }

        return prochaineHypothese;
    }

    /**
     * @return le coût de l'ajustement actuel
     */
    float coutAjustementActuel() {
        return coutActuel;
    }

    /**
     * appelé pour récupérer le meilleur clustering quand on a fini d'équilibrer
     * @return les combos regroupés par cluster
     */
    List<ClusterDeBase<ComboPreClustering>> clusteringActuel() {
        List<ClusterRange> clustersFinaux = trouverClusters(valeursAjustementsCourantes);

        return new ArrayList<>(clustersFinaux);
    }


    // méthodes privées d'ajustement interne

    /**
     * initialise les valeurs de l'ajustement
     * crée des bornes réparties uniformément sur la plage de valeurs
     * @param hypotheses nombre de classes qu'on veut par composante
     */
    private void initialiserValeurs(int[] hypotheses) {
        if (minValeurs == null || maxValeurs == null)
            throw new RuntimeException("Valeurs min et max pas initialisées");

        valeursAjustementsCourantes = new float[hypotheses.length][];
        for (int i = 0; i < hypotheses.length; i++) {
            // il y a une borne de plus car on veut 0 et max et dans les bornes
            int nBornes = hypotheses[i] + 1;
            valeursAjustementsCourantes[i] = new float[nBornes];

            for (int j = 0; j < nBornes; j++) {
                float valeurInitiale = (j * ((maxValeurs[i] - minValeurs[i]) / hypotheses.length)) + minValeurs[i];
                valeursAjustementsCourantes[i][j] = valeurInitiale;
            }
        }
    }

    /**
     * calcul le cout actuel d'une hypothèse donnée de valeurs de bornes
     * plus le coût est élevé moins l'hypothèse est bonne
     */
    private float calculerCout(float[][] hypotheseBornes) {
        // on regroupe les combos
        List<ClusterRange> clustersHypothese = trouverClusters(hypotheseBornes);

        HashMap<ClusterRange, Float> homogeneites = new HashMap<>();

        float coutTotal = 0f;
        for (ClusterRange cluster : clustersHypothese) {
            // si un cluster est vide pire des situations, erreur maximale
            if (cluster.getEffectif() == 0) return Float.MAX_VALUE;

            // on calcule la qualité du cluster
            float qualiteCluster = indiceDaviesBouldin(cluster, clustersHypothese, homogeneites);

            // on calcule l'indice lié à son effectif
            float coutNombreServis = coutNombreServis(cluster);

            float coutCluster = fonctionCout(qualiteCluster, coutNombreServis);

            coutTotal += coutCluster;
        }

        return coutTotal / clustersHypothese.size();
    }


    /**
     * calcule le cout d'un cluster lié à son nombre de combos servis
     * compris entre (0.9 => pénalité forte et 0.1 => pénalité faible)
     * @param cluster le cluster concerné
     * @return la valeur de pénalité
     */
    private float coutNombreServis(ClusterRange cluster) {
        float nCombosServis = 0f;
        for (ComboPreClustering combo : cluster.getObjets()) {
            nCombosServis += combo.getPCombo() * nObservations;
        }

        return (float) coutCombosServis.getValeur(nCombosServis);
    }

    /**
     * calcule le qualité d'un cluster relativement aux autres
     * ***************************************
     * la formule globale est pour chaque cluster k
     * Dk = max(k≠l) ( (Tl + Tk) / S(k,l) )
     * avec : Tk => distance moyenne au centroide des points du cluster
     * S(k, l) distance entre les centroides de k et l
     * **********************************
     * compris entre 0 => bon clustering et +infini => mauvais clustering
     *
     * @param cluster           le cluster concerné
     * @param clustersHypothese les autres clusters (y compris lui-même)
     * @param homogeneites la map qui stocke les valeurs pour ne pas recalculer
     * @return la valeur de pénalité
     */
    private float indiceDaviesBouldin(ClusterRange cluster,
                                      List<ClusterRange> clustersHypothese,
                                      HashMap<ClusterRange, Float> homogeneites) {

        float homogeneite = recupererHomogeneite(cluster, homogeneites);

        float maxK = Float.MIN_VALUE;
        for (ClusterRange autreCluster : clustersHypothese) {
            if (cluster == autreCluster) continue;
            float homogeneiteAutreCluster = recupererHomogeneite(autreCluster, homogeneites);
            float distanceCentroides = cluster.distance(autreCluster);

            float valeurK = (homogeneiteAutreCluster + homogeneite) / distanceCentroides;
            maxK = Math.max(valeurK, maxK);
        }

        return maxK;
    }

    /**
     * gestion d'une map des valeurs pour éviter de nouveaux calculs couteux à chaque tour
     * @param cluster le cluster pour lequel on veut l'homogénéité
     * @param homogeneites la map qui stocke les valeurs, la récupère ou la crée si n'existe pas
     * @return la valeur de l'homogénéité
     */
    private float recupererHomogeneite(ClusterRange cluster,
                                       HashMap<ClusterRange, Float> homogeneites) {
        Float valeurStockee = homogeneites.get(cluster);
        if (valeurStockee == null) {
            valeurStockee = cluster.homogeneite();
            homogeneites.put(cluster, valeurStockee);
        }

        return valeurStockee;
    }

    /**
     * calcule un cout total lié à la qualité du cluster et son nombre d'observations
     * plus c'est faibble mieux c'est (compris entre 0 et +infini)
     * @param qualiteCluster la qualité du clustering
     * @param coutNombreServis le cout lié aux nombres d'observations du cluster
     * @return une valeur synthétique
     */
    private float fonctionCout(float qualiteCluster, float coutNombreServis) {
        return (float) (qualiteCluster * Math.pow(coutNombreServis, POIDS_N_SERVIS));
    }

    /**
     * va construire les clusters selon une hypothèse donnée
     * @return les combos regroupés dans des clusters
     */
    private List<ClusterRange> trouverClusters(float[][] hypotheseBornes) {
        List<ClusterRange> clustersSepares = null;

        // on va séparer de manière récursive les clusters par composante
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

    /**
     * méthode pour séparer des clusters
     * @param clustersOrigine liste des clusters à splitter
     * @param indexComposante index de la composante qu'on veut séparer
     * @param bornesHypothese les bornes des hypothèses (sans 0 et max valeurs)
     * @return une liste de clusters séparés
     */
    private List<ClusterRange> separerClusters(
            List<ClusterRange> clustersOrigine,
            int indexComposante,
            float[] bornesHypothese) {
        // si vaut null, première itération, on va créer les premiers clusters
        if (clustersOrigine == null) {
            clustersOrigine = new ArrayList<>();
            clustersOrigine.add(new ClusterRange(combos));
        }

        List<ClusterRange> clustersFinaux = new ArrayList<>();

        for (ClusterRange clusterOriginal : clustersOrigine) {
            // on crée une map pour mettre les combos dans le bon cluster
            // on rajoute deux clusters pour les valeurs inférieures et supérieures
            HashMap<Integer, ClusterRange> mapClusters = new HashMap<>();
            for (int i = 0; i < (bornesHypothese.length - 1); i++) {
                mapClusters.put(i, new ClusterRange());
            }

            // pour chaque cluster original, on loop sur les objets qu'il contient
            for (ComboPreClustering comboCluster : clusterOriginal.getObjets()) {
                Integer indexMap = null;
                float valeurStockee = comboCluster.valeursNormalisees()[indexComposante];

                for (int j = 0; j < bornesHypothese.length - 1; j++) {
                    if (valeurStockee > bornesHypothese[j] && valeurStockee < bornesHypothese[j + 1]) {
                        indexMap = j;
                        break;
                    }
                }
                if (indexMap == null) throw new RuntimeException("Aucune index trouvé");

                mapClusters.get(indexMap).ajouterObjet(comboCluster);
            }

            clustersFinaux.addAll(mapClusters.values());
        }

        return clustersFinaux;
    }

    /**
     * valeur sigmoide inverse custom pour mapper entre valeurs min et max
     * en xMin => valeur retournée = 0.88
     * en xMax => valeur retournée 0.12
     * plafond entre 0.9 et 0.1
     */
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
