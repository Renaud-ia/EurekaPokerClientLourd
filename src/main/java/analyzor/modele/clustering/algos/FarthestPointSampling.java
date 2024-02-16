package analyzor.modele.clustering.algos;

import analyzor.modele.clustering.objets.ComboPreClustering;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * implémentation naïve de Farthest Point Sampling
 * pour choisir des centres de densite, on va juste prendre le point le plus éloigné de tous les autres à chaque fois
 * todo intégrer la variance intra-clusters comme critère ?
 */
@Deprecated
public class FarthestPointSampling {
    private final static Logger logger = LogManager.getLogger();
    // nombre de points random qu'on va tester en hypothèse
    private static final float PCT_HYPOTHESES = 0.5f;
    private final int minClusters;
    private final int maxClusters;
    private final float poidsVarianceIntra;
    private final float poidsDistanceInter;
    private float coutActuel;
    private List<ComboPreClustering> meilleursCentresDeGravite;
    private List<ComboPreClustering> pointsInitiaux;
    /**
     * initialisation
     * @param minClusters nombre minimum de clusters qu'on veut tester
     * @param maxClusters nombre maximum de clusters qu'on veut tester
     * @param poidsVarianceIntra poids de la variance intra cluster dans la fonction coût
     * @param poidsDistanceInter poids de la distance inter cluster dans la fonction coût
     */
    public FarthestPointSampling(int minClusters, int maxClusters, float poidsVarianceIntra, float poidsDistanceInter) {
        this.minClusters = minClusters;
        this.maxClusters = maxClusters;
        this.poidsVarianceIntra = poidsVarianceIntra;
        this.poidsDistanceInter = poidsDistanceInter;
    }

    public void ajouterDonnees(List<ComboPreClustering> pointsInitiaux) {
        this.pointsInitiaux = pointsInitiaux;
    }

    public List<ComboPreClustering> meilleureHypothese() {
        List<ComboPreClustering> meilleureHypothese = new ArrayList<>();
        float coutPlusFaible = Float.MAX_VALUE;

        for (int n_clusters = minClusters; n_clusters <= maxClusters; n_clusters++) {
            float cout = samplerHypothese(n_clusters);
            if (cout < coutPlusFaible) {
                coutPlusFaible = cout;
                meilleureHypothese = meilleursCentresDeGravite;
            }

            logger.trace("On teste l'hypothèse avec n_clusters = " + n_clusters);
            logger.trace("Cout plus faible : " + cout);
        }

        return meilleureHypothese;
    }

    /**
     * algorithme qui teste plusieurs combinaisons de point selon % spécifié pour n_clusters
     * @param nClusters nombre de clusters voulus
     * @return le coût de la meilleure combinaison trouvée
     */
    private float samplerHypothese(int nClusters) {
        int nTentatives = (int) (PCT_HYPOTHESES * pointsInitiaux.size());
        float coutPlusFaible = Float.MAX_VALUE;
        for (int i = 0; i < nTentatives; i++) {
            List<ComboPreClustering> centreGravite = samplerPoints(nClusters);
            final float cout = calculerCout(centreGravite);
            if (cout < coutPlusFaible) {
                coutPlusFaible = cout;
                meilleursCentresDeGravite = centreGravite;
            }
            logger.trace("Tentative n°" + i + ", le cout est de : " + cout);
        }

        return coutPlusFaible;
    }


    /**
     * coeur de l'algo implémentation de FPS
     * @param nClusters nombre de clusters qu'on veut
     * @return la liste des centres de gravité choisis
     */
    private List<ComboPreClustering> samplerPoints(int nClusters) {
        // on choisit un premier point au hasard
        Random random = new Random();
        final int indexRandom = random.nextInt(0, pointsInitiaux.size());
        ComboPreClustering centreInitial = pointsInitiaux.get(indexRandom);

        // on initialise les centres
        List<ComboPreClustering> centresTrouves = new ArrayList<>();
        centresTrouves.add(centreInitial);
        logger.trace("Centre initial : " + centreInitial.getNoeudEquilibrage());

        // on prend à chaque fois le point le plus éloigné des autres
        for(int i = 0; i < nClusters; i++) {
            ComboPreClustering prochainCentre = centrePlusEloigne(centresTrouves);
            centresTrouves.add(prochainCentre);
            logger.trace("Point plus éloigné : " + prochainCentre.getNoeudEquilibrage());
        }

        return centresTrouves;
    }

    private ComboPreClustering centrePlusEloigne(List<ComboPreClustering> centresActuels) {
        float distancePlusEloignee = Float.MIN_VALUE;
        ComboPreClustering comboPlusLoin = null;

        for (ComboPreClustering autrePoint : pointsInitiaux) {
            if (centresActuels.contains(autrePoint)) continue;
            final float distance = distanceAvecAutresPoints(centresActuels, autrePoint);
            if (distance > distancePlusEloignee) {
                distancePlusEloignee = distance;
                comboPlusLoin = autrePoint;
            }
        }

        if (comboPlusLoin == null) throw new RuntimeException("Aucun point plus loin trouvé");

        return comboPlusLoin;
    }

    /**
     * distance moyenne avec un ensemble d'autres points
     * @param groupePoints points avec lesquels on veut mesurer la distance
     * @param pointMesure point qu'on mesure
     * @return la distance moyenne
     */
    private float distanceAvecAutresPoints(List<ComboPreClustering> groupePoints, ComboPreClustering pointMesure) {
        float distanceTotale = 0f;
        for (ComboPreClustering point : groupePoints) {
            if (point == pointMesure) throw new IllegalArgumentException("Le point mesuré est dans le groupe de points");
            distanceTotale += pointMesure.distance(point);
        }
        return distanceTotale / groupePoints.size();
    }

    /**
     * fonction cout
     * @param centresGravite liste des centres retenus
     * @return le négatif de la distance moyenne intercluster car on veut maximiser cette valeur
     */
    private float calculerCout(List<ComboPreClustering> centresGravite) {
        return - distanceInterCluster(centresGravite);
    }

    /**
     * distance moyenne inter clusters
     * @param centresGravite points retenus
     * @return la distance moyenne inter-cluster
     */
    private float distanceInterCluster(List<ComboPreClustering> centresGravite) {
        float distanceTotale = 0f;
        int compte = 0;

        for (ComboPreClustering centre1 : centresGravite) {
            for (ComboPreClustering centre2 : centresGravite) {
                distanceTotale += centre1.distance(centre2);
                compte++;
            }
        }

        return distanceTotale / compte;
    }
}
