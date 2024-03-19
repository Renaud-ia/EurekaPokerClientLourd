package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.FarthestPointSampling;
import analyzor.modele.clustering.cluster.ClusterDeBase;
import analyzor.modele.clustering.objets.ComboPostClustering;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * méthode innovante pour clusteriser les ranges
 * on essaie d'identifier des centres de gravité stratégiques de la range
 * tient compte du nombre de combos dans la range
 *
 *
 */
public class SpecialRange {
    private static final Logger logger = LogManager.getLogger(SpecialRange.class);
    // todo trouver les meilleurs valeurs
    private static final int MIN_OBSERVATIONS_PAR_CENTRE = 300;
    private static final int MIN_CENTRES_GRAVITE = 3;
    private static final int MAX_CENTRES_GRAVITE = 15;
    private static final float SEUIL_FRONTIERE = 0.7f;
    private final int nSituations;
    private List<NoeudEquilibrage> noeudInitiaux;
    private final List<ComboPostClustering> pointsIsoles;
    private final HashMap<ComboPostClustering, List<ComboPostClustering>> pointsAttribues;
    private final List<ClusterEquilibrage> clustersFinaux;

    public SpecialRange(int nSituations) {
        this.nSituations = nSituations;
        pointsIsoles = new ArrayList<>();
        clustersFinaux = new ArrayList<>();
        pointsAttribues = new HashMap<>();
    }

    // interface publique pour lancer le clustering

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        noeudInitiaux = noeuds;
    }


    public void lancerClustering() {
        // on récupère les centres de gravité
        List<ComboPostClustering> centresGravites = trouverCentresGravites();

        logger.trace("Nombre de centres de gravité : " + centresGravites.size());

        // on attribue chaque point isole à un centre de gravité"
        attribuerPointsIsoles(centresGravites);
    }

    public List<ClusterEquilibrage> getResultats() {
        return clustersFinaux;
    }

    // clustering de la range

    private List<ComboPostClustering> trouverCentresGravites() {
        // effectif minimum pour être considéré comme un centre
        final int MIN_EFFECTIF_CENTRE = 3;
        // on clusterise la range par clustering hiérarchique
        KMeansRange preClustering = new KMeansRange(nSituations);
        preClustering.ajouterDonnees(noeudInitiaux);
        preClustering.lancerClustering();

        List<ClusterDeBase<ComboPreClustering>> centresGravites = preClustering.getCentresGravites();

        List<ComboPostClustering> centresFinaux = new ArrayList<>();


        for (ClusterDeBase<ComboPreClustering> cluster : centresGravites) {
            // on met les centres dans un groupe
            ComboPreClustering centre = cluster.getCentreCluster();
            ComboPostClustering centreGravite = new ComboPostClustering(centre.getNoeudEquilibrage());
            centresFinaux.add(centreGravite);
            logger.info("Centre gravité initialisé : " + centre.getNoeudEquilibrage());


            // on répertorie les points isolés = autres points du cluster
            for (ComboPreClustering membreCluster : cluster.getObjets()) {
                if (membreCluster == centre) continue;
                ComboPostClustering comboPostClustering = new ComboPostClustering(membreCluster.getNoeudEquilibrage());
                pointsIsoles.add(comboPostClustering);
            }
        }

        return centresFinaux;
    }

    // extension des clusters

    /**
     * formation des clusters à partir des centres de gravité
     * si un point appartient clairement à un cluster en termes d'équite, on l'attribue
     * s'il est limite, on prend équite et probabilités pour trancher
     * @param centresGravites : centres de gravité calculés précédemment
     */
    private void attribuerPointsIsoles(List<ComboPostClustering> centresGravites) {
        // on crée la HashMap pour attribuer les points isolés
        for (ComboPostClustering centreGravite : centresGravites) {
            pointsAttribues.put(centreGravite, new ArrayList<>());
        }

        // on parcout les points isolés
        for (ComboPostClustering pointIsole : pointsIsoles) {
            // on essaye de leur trouver un centre plus proche en termes d'équité seulement
            if (trouverCentrePlusProche(pointIsole, centresGravites)) continue;

            // sinon on prend une méthode hybride
            attribuerCentre(pointIsole, centresGravites);
        }

        creerClustersFinaux();
    }

    /**
     * méthode pour trouver le centre le plus proche en terme d'équité
     * @return true si on a réussi à trouver sinon faux
     */
    private boolean trouverCentrePlusProche(ComboPostClustering pointIsole, List<ComboPostClustering> centresGravites) {
        // on garde les deux distances plus proches
        float minDistance = Float.MAX_VALUE;
        float secondeMinDistance = Float.MAX_VALUE;

        ComboPostClustering centrePlusProche = null;

        // on regarde la distance d'équité avec les centres
        for (ComboPostClustering centreGravite : centresGravites) {
            float distance = centreGravite.distanceEquite(pointIsole);
            if (distance < minDistance) {
                minDistance = distance;
                centrePlusProche = centreGravite;
            }

            else if (distance < secondeMinDistance) {
                secondeMinDistance = distance;
            }
        }

        if (centrePlusProche == null) throw new RuntimeException("Aucun centre plus proche trouvé");

        // on vérifie que la différence de distance est significative
        if ((minDistance / secondeMinDistance) < SEUIL_FRONTIERE) {
            pointsAttribues.get(centrePlusProche).add(pointIsole);

            logger.trace("Centre de gravité plus proche (équité) pour " + pointIsole.getNoeudEquilibrage() +
                    "trouvé : " + centrePlusProche.getNoeudEquilibrage());

            return true;
        }

        return false;
    }

    /**
     * autre méthode pour attribuer centre à travers une distance pondérée entre équité et probabilités
     */
    private void attribuerCentre(ComboPostClustering pointIsole, List<ComboPostClustering> centresGravites) {
        // on garde les deux distances plus proches
        float minDistance = Float.MAX_VALUE;

        ComboPostClustering centrePlusProche = null;

        // on regarde la distance d'équité avec les centres
        for (ComboPostClustering centreGravite : centresGravites) {
            float distance = centreGravite.distancePonderee(pointIsole);
            if (distance < minDistance) {
                minDistance = distance;
                centrePlusProche = centreGravite;
            }
        }

        if (centrePlusProche == null) throw new RuntimeException("Aucun centre plus proche trouvé");
        pointsAttribues.get(centrePlusProche).add(pointIsole);

        logger.trace("Centre de gravité plus proche (hybride) pour " + pointIsole.getNoeudEquilibrage() +
                "trouvé : " + centrePlusProche.getNoeudEquilibrage());
    }

    /**
     * juste convertir au bon format les objets
     */
    private void creerClustersFinaux() {
        // on parcout la HashMap
        // on crée des clusters equilibrages

        for (ComboPostClustering centreGravite : pointsAttribues.keySet()) {
            List<ComboPostClustering> points = pointsAttribues.get(centreGravite);

            List<NoeudEquilibrage> noeudsCluster = new ArrayList<>();

            noeudsCluster.add(centreGravite.getNoeudEquilibrage());
            for (ComboPostClustering pointCluster : points) {
                noeudsCluster.add(pointCluster.getNoeudEquilibrage());
            }

            ClusterEquilibrage clusterEquilibrage = new ClusterEquilibrage(noeudsCluster);
            clustersFinaux.add(clusterEquilibrage);

        }
    }


}
