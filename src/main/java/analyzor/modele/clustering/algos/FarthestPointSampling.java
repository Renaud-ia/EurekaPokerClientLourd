package analyzor.modele.clustering.algos;

import analyzor.modele.clustering.objets.ComboPreClustering;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Deprecated
public class FarthestPointSampling {
    private final static Logger logger = LogManager.getLogger(FarthestPointSampling.class);
    
    private static final float PCT_HYPOTHESES = 0.5f;
    private final int minClusters;
    private final int maxClusters;
    private final float poidsVarianceIntra;
    private final float poidsDistanceInter;
    private float coutActuel;
    private List<ComboPreClustering> meilleursCentresDeGravite;
    private List<ComboPreClustering> pointsInitiaux;
    
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


    
    private List<ComboPreClustering> samplerPoints(int nClusters) {
        
        Random random = new Random();
        final int indexRandom = random.nextInt(0, pointsInitiaux.size());
        ComboPreClustering centreInitial = pointsInitiaux.get(indexRandom);

        
        List<ComboPreClustering> centresTrouves = new ArrayList<>();
        centresTrouves.add(centreInitial);
        logger.trace("Centre initial : " + centreInitial.getNoeudEquilibrage());

        
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

    
    private float distanceAvecAutresPoints(List<ComboPreClustering> groupePoints, ComboPreClustering pointMesure) {
        float distanceTotale = 0f;
        for (ComboPreClustering point : groupePoints) {
            if (point == pointMesure) throw new IllegalArgumentException("Le point mesuré est dans le groupe de points");
            distanceTotale += pointMesure.distance(point);
        }
        return distanceTotale / groupePoints.size();
    }

    
    private float calculerCout(List<ComboPreClustering> centresGravite) {
        return - distanceInterCluster(centresGravite);
    }

    
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
