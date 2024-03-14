package analyzor.modele.clustering.range;

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
 * interface avec le reste du projet
 * trouve la meilleure division de range lié à l'équite et aux observations
 * construit l'ACP, appelle l'optimiseur d'hypothèse
 * nettoie les clusters et les renvoie
 */
public class ClusteringDivisifRange {
    private final static Logger logger = LogManager.getLogger(ClusteringDivisifRange.class);
    private static final float SEUIL_FRONTIERE = 0.9f;
    private final OptimiseurHypothese optimiseurHypothese;
    private List<NoeudEquilibrage> noeudsInitiaux;
    private final List<ComboPostClustering> pointsIsoles;
    private final HashMap<ComboPostClustering, List<ComboPostClustering>> pointsAttribues;
    private final List<ClusterEquilibrage> clustersFinaux;
    public ClusteringDivisifRange(int nSituations) {
        optimiseurHypothese = new OptimiseurHypothese(nSituations);
        pointsIsoles = new ArrayList<>();
        pointsAttribues = new HashMap<>();
        clustersFinaux = new ArrayList<>();
    }
    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        this.noeudsInitiaux = noeuds;

        // on crée l'ACP
        AcpRange acpRange = new AcpRange();
        acpRange.ajouterDonnees(noeuds);
        acpRange.transformer();
        // important, il nous faut un nouveau type d'objet car il stocke les valeurs transformées par l'ACP
        List<ComboPreClustering> donneesTransformees = acpRange.getDonnesTransformees();

        // on dit à l'optimiseur de créer les hypothèses
        optimiseurHypothese.creerHypotheses(donneesTransformees);
    }

    public List<ClusterEquilibrage> getResultats() {
        List<ComboPostClustering> meilleureHypothese = optimiseurHypothese.meilleureHypothese();

        logger.debug("Clustering terminé");

        return etendreLesCentres(meilleureHypothese);
    }

    /**
     * va étendre les centres de gravité
     * @param centresGravite combos qui cosntituent les centres de gravité de la range
     * @return des clusters formés et prêts à être équilibrés
     */
    private List<ClusterEquilibrage> etendreLesCentres(List<ComboPostClustering> centresGravite) {
        // on identifie les points isolés et on les mets dans le bon objet
        for (NoeudEquilibrage noeudEquilibrage : noeudsInitiaux) {
            boolean estCentre = false;
            for (ComboPostClustering centre : centresGravite) {
                if (centre.getNoeudEquilibrage() == noeudEquilibrage) {
                    estCentre = true;
                    break;
                }
            }

            if (!estCentre) {
                ComboPostClustering comboPostClustering = new ComboPostClustering(noeudEquilibrage);
                pointsIsoles.add(comboPostClustering);
            }
        }

        attribuerPointsIsoles(centresGravite);

        return clustersFinaux;
    }

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
