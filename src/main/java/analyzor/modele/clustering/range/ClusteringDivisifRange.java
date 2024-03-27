package analyzor.modele.clustering.range;

import analyzor.modele.clustering.objets.ComboPostClustering;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.denombrement.CalculEquitePreflop;
import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.denombrement.combos.DenombrableIso;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import analyzor.modele.estimation.CalculInterrompu;
import analyzor.modele.estimation.Estimateur;
import analyzor.modele.poker.ComboIso;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * interface avec le reste du projet
 * trouve la meilleure division de range lié à l'équite et aux observations
 * construit l'ACP, appelle l'optimiseur d'hypothèse
 * nettoie les clusters et les renvoie
 */
public class ClusteringDivisifRange {
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

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) throws CalculInterrompu {
        this.noeudsInitiaux = noeuds;

        if (Estimateur.estInterrompu()) throw new CalculInterrompu();

        // on crée l'ACP
        AcpRange acpRange = new AcpRange();
        acpRange.ajouterDonnees(noeuds);
        acpRange.transformer();
        // important, il nous faut un nouveau type d'objet car il stocke les valeurs transformées par l'ACP
        List<ComboPreClustering> donneesTransformees = acpRange.getDonnesTransformees();

        // on dit à l'optimiseur de créer les hypothèses
        optimiseurHypothese.creerHypotheses(donneesTransformees);
    }


    public List<ClusterEquilibrage> getResultats() throws CalculInterrompu {
        List<ComboPostClustering> meilleureHypothese = optimiseurHypothese.meilleureHypothese();


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
            trouverCentrePlusProche(pointIsole, centresGravites);
        }

        creerClustersFinaux();
    }


    /**
     * méthode pour trouver le centre le plus proche en terme d'équité
     */
    private void trouverCentrePlusProche(ComboPostClustering pointIsole, List<ComboPostClustering> centresGravites) {
        // on garde les deux distances plus proches
        float minDistance = Float.MAX_VALUE;

        ComboPostClustering centrePlusProche = null;

        // on regarde la distance d'équité avec les centres
        for (ComboPostClustering centreGravite : centresGravites) {
            float distance = centreGravite.distanceEquite(pointIsole);
            if (distance < minDistance) {
                minDistance = distance;
                centrePlusProche = centreGravite;
            }
        }

        if (centrePlusProche == null) throw new RuntimeException("Aucun centre plus proche trouvé");

        pointsAttribues.get(centrePlusProche).add(pointIsole);
    }

    /**
     * juste convertir au bon format les objets
     * et nettoyer les clusters qui doivent l'être
     */
    private void creerClustersFinaux() {
        // on parcout la HashMap
        // on crée des clusters equilibrages

        List<List<NoeudEquilibrage>> clustersFormes = new ArrayList<>();

        for (ComboPostClustering centreGravite : pointsAttribues.keySet()) {
            List<ComboPostClustering> points = pointsAttribues.get(centreGravite);

            List<NoeudEquilibrage> noeudsCluster = new ArrayList<>();

            noeudsCluster.add(centreGravite.getNoeudEquilibrage());
            for (ComboPostClustering pointCluster : points) {
                noeudsCluster.add(pointCluster.getNoeudEquilibrage());
            }

            clustersFormes.add(noeudsCluster);

        }

        reattribuerPetitesPp(clustersFormes);

        for (List<NoeudEquilibrage> cluster : clustersFormes) {
            ClusterEquilibrage clusterEquilibrage = new ClusterEquilibrage(cluster);
            clustersFinaux.add(clusterEquilibrage);
        }
    }

    /**
     * méthode custom pour réaffecter après coup les petites pp car Equite Future modélise très mal leur comportement
     * @param clustersFormes les clusters déjà formés prêts à être transmis à l'équilibrage
     */
    private void reattribuerPetitesPp(List<List<NoeudEquilibrage>> clustersFormes) {
        // d'abord on trouve le cluster d'accueil
        List<NoeudEquilibrage> clusterAccueil = null;
        outerLoop:
        for (List<NoeudEquilibrage> cluster : clustersFormes) {
            for (NoeudEquilibrage noeudIsole : cluster) {
                ComboIso comboIsole = ((DenombrableIso) ((ComboIsole) noeudIsole).getComboDenombrable()).getCombo();
                if (comboIsole.equals(CalculEquitePreflop.comboReferent)) {
                    clusterAccueil = cluster;
                    break outerLoop;
                }
            }
        }

        if (clusterAccueil == null) throw new RuntimeException("Cluster accueil non trouvé");

        // puis on cherche toutes les pp qui doivent changer et on les réaffecte
        List<NoeudEquilibrage> noeudsChangesDeCluster = new ArrayList<>();
        for (List<NoeudEquilibrage> cluster : clustersFormes) {
            Iterator<NoeudEquilibrage> iterateur = cluster.iterator();
            while(iterateur.hasNext()) {
                NoeudEquilibrage noeudIsole = iterateur.next();
                ComboIso comboIsole = ((DenombrableIso) ((ComboIsole) noeudIsole).getComboDenombrable()).getCombo();
                if (CalculEquitePreflop.ppDistanceSpeciale.contains(comboIsole)) {
                    noeudsChangesDeCluster.add(noeudIsole);
                    iterateur.remove();
                }
            }
        }

        clusterAccueil.addAll(noeudsChangesDeCluster);
    }
}
