package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.clustering.objets.ComboEquite;
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
    private static final int MAX_CENTRES_GRAVITE = 7;
    private static final float SEUIL_FRONTIERE = 0.7f;
    private final int nSituations;
    private int nCentresGravite;
    private final List<ComboEquite> combosParEquite;
    private final HashMap<ComboEquite, List<ComboEquite>> pointsAttribues;
    private final List<ClusterEquilibrage> clustersFinaux;

    public SpecialRange(int nSituations) {
        this.nSituations = nSituations;
        combosParEquite = new ArrayList<>();
        clustersFinaux = new ArrayList<>();
        pointsAttribues = new HashMap<>();
    }

    // interface publique pour lancer le clustering

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        // todo prendre plus de candidat que de centres de gravité finaux
        // todo puis élaguer pour atteindre le nombre souhaité
        // on définit le nombre de centres de gravité
        definirNCentresGravite();

        // on insère les combos dans un objet clusterisable
        for (NoeudEquilibrage noeudEquilibrage : noeuds) {
            ComboEquite comboEquite = new ComboEquite(noeudEquilibrage);
            combosParEquite.add(comboEquite);
        }
    }


    public void lancerClustering() {
        // on récupère les centres de gravité
        List<ComboEquite> centresGravites = trouverCentresGravites();

        // on attribue chaque point isole à un centre de gravité"
        attribuerPointsIsoles(centresGravites);
    }

    public List<ClusterEquilibrage> getResultats() {
        return clustersFinaux;
    }


    // méthodes privées de logique du clustering

    /**
     * on affecte un nombre de centres de gravité qu'on veut
     */
    private void definirNCentresGravite() {
        int nCentresGravite = nSituations / MIN_OBSERVATIONS_PAR_CENTRE;
        nCentresGravite = Math.max(nCentresGravite, MIN_CENTRES_GRAVITE);
        nCentresGravite = Math.min(nCentresGravite, MAX_CENTRES_GRAVITE);
        this.nCentresGravite = nCentresGravite;
        logger.trace("Nombre de centres de gravité fixés : " + nCentresGravite);
    }

    // clustering de la range

    private List<ComboEquite> trouverCentresGravites() {
        // on clusterise la range par KMeans sur équité future
        List<ClusterKMeans<ComboEquite>> clusters = clusteriserRangeParEquite();

        List<ComboEquite> centresGravites = new ArrayList<>();
        // on prend les points les plus représentatifs en termes de probabilités (=stratégie)
        // distance moyenne avec les autres points => centres de densité locaux
        // moins sensibles aux valeurs aberrantes
        // on pourrait également prendre les centroides mais pas forcément pertinent
        for (ClusterKMeans<ComboEquite> cluster : clusters) {
            float minDistanceMoyenne = Float.MAX_VALUE;
            ComboEquite centreTrouve = null;

            for (ComboEquite comboEquite : cluster.getObjets()) {
                float distanceMoyenne = 0;
                for (ComboEquite comboEquiteVoisin : cluster.getObjets()) {
                    if (comboEquiteVoisin == comboEquite) continue;
                    distanceMoyenne += comboEquite.distanceProbabilites(comboEquiteVoisin);
                }
                distanceMoyenne /= cluster.getEffectif() - 1;

                if (distanceMoyenne < minDistanceMoyenne) {
                    minDistanceMoyenne = distanceMoyenne;
                    centreTrouve = comboEquite;
                }
            }

            if (centreTrouve == null) throw new RuntimeException("Aucun centre trouvé");
            logger.trace("Centre gravité trouvé : " + centreTrouve.getNoeudEquilibrage());
            centresGravites.add(centreTrouve);
        }

        return centresGravites;
    }

    /**
     * on fait juste tourner un KMEANS sur l'équité avec nombre de centres de gravité qu'on veut
     * @return les combos groupés par clusters
     */
    private List<ClusterKMeans<ComboEquite>> clusteriserRangeParEquite() {
        ClusteringKMeans<ComboEquite> kMeans = new ClusteringKMeans<>();
        kMeans.initialiser(combosParEquite);
        kMeans.ajusterClusters(nCentresGravite);

        return kMeans.getClusters();
    }

    // extension des clusters

    /**
     * formation des clusters à partir des centres de gravité
     * si un point appartient clairement à un cluster en termes d'équite, on l'attribue
     * s'il est limite, on prend équite et probabilités pour trancher
     * @param centresGravites : centres de gravité calculés précédemment
     */
    private void attribuerPointsIsoles(List<ComboEquite> centresGravites) {
        // on crée la HashMap pour attribuer les points isolés
        for (ComboEquite centreGravite : centresGravites) {
            pointsAttribues.put(centreGravite, new ArrayList<>());
        }

        // on parcout les points isolés
        for (ComboEquite pointIsole : combosParEquite) {
            if (centresGravites.contains(pointIsole)) continue;

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
    private boolean trouverCentrePlusProche(ComboEquite pointIsole, List<ComboEquite> centresGravites) {
        // on garde les deux distances plus proches
        float minDistance = Float.MAX_VALUE;
        float secondeMinDistance = Float.MAX_VALUE;

        ComboEquite centrePlusProche = null;

        // on regarde la distance d'équité avec les centres
        for (ComboEquite centreGravite : centresGravites) {
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
    private void attribuerCentre(ComboEquite pointIsole, List<ComboEquite> centresGravites) {
        // on garde les deux distances plus proches
        float minDistance = Float.MAX_VALUE;

        ComboEquite centrePlusProche = null;

        // on regarde la distance d'équité avec les centres
        for (ComboEquite centreGravite : centresGravites) {
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

        for (ComboEquite centreGravite : pointsAttribues.keySet()) {
            List<ComboEquite> points = pointsAttribues.get(centreGravite);

            List<NoeudEquilibrage> noeudsCluster = new ArrayList<>();

            noeudsCluster.add(centreGravite.getNoeudEquilibrage());
            for (ComboEquite pointCluster : points) {
                noeudsCluster.add(pointCluster.getNoeudEquilibrage());
            }

            ClusterEquilibrage clusterEquilibrage = new ClusterEquilibrage(noeudsCluster);
            clustersFinaux.add(clusterEquilibrage);

        }
    }


}
