package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.ClusteringKMeans;
import analyzor.modele.clustering.cluster.ClusterKMeans;
import analyzor.modele.clustering.objets.ComboEquite;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
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
    private final int nSituations;
    private List<NoeudEquilibrage> noeudEquilibrages;
    private int nCentresGravite;
    public SpecialRange(int nSituations) {
        this.nSituations = nSituations;
    }

    // interface publique pour lancer le clustering

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        // on définit le nombre de centres de gravité
        definirNCentresGravite();
        noeudEquilibrages = noeuds;
    }


    public void lancerClustering() {
        // on récupère les centres de gravité
        List<NoeudEquilibrage> centresGravites = trouverCentresGravites();

        // on les étend par équité future jusqu'à arriver aux frontières des clusters

        // pour les points limites, on prend équite ET probabilités
    }


    // méthodes privées de logique du clustering

    /**
     * on affecte un nombre de centres de gravité qu'on veut
     */
    private void definirNCentresGravite() {
        int nCentresGravite = nSituations / MIN_OBSERVATIONS_PAR_CENTRE;
        nCentresGravite = Math.min(nCentresGravite, MIN_CENTRES_GRAVITE);
        nCentresGravite = Math.max(nCentresGravite, MAX_CENTRES_GRAVITE);
        this.nCentresGravite = nCentresGravite;
        logger.trace("Nombre de centres de gravité fixés : " + nCentresGravite);
    }

    // clustering de la range

    private List<NoeudEquilibrage> trouverCentresGravites() {
        // on clusterise la range par KMeans sur équité future
        List<ClusterKMeans<ComboEquite>> clusters = clusteriserRangeParEquite();

        List<NoeudEquilibrage> centresGravites = new ArrayList<>();
        // on prend les points les plus représentatifs en termes de probabilités (=stratégie)
        // distance moyenne avec les autres points => centres de densité locaux
        // moins sensibles aux valeurs aberrantes
        // on pourrait également prendre les centroides mais pas forcément pertinent
        for (ClusterKMeans<ComboEquite> cluster : clusters) {
            float minDistanceMoyenne = Float.MIN_VALUE;
            NoeudEquilibrage centreTrouve = null;

            for (ComboEquite comboEquite : cluster.getObjets()) {
                float distanceMoyenne = 0;
                for (ComboEquite comboEquiteVoisin : cluster.getObjets()) {
                    if (comboEquiteVoisin == comboEquite) continue;
                    distanceMoyenne += comboEquite.distanceProbabilites(comboEquiteVoisin);
                }
                distanceMoyenne /= cluster.getEffectif() - 1;

                if (distanceMoyenne > minDistanceMoyenne) {
                    minDistanceMoyenne = distanceMoyenne;
                    centreTrouve = comboEquite.getNoeudEquilibrage();
                }
            }

            if (centreTrouve == null) throw new RuntimeException("Aucun centre trouvé");
            centresGravites.add(centreTrouve);
        }

        return centresGravites;
    }

    /**
     * on fait juste tourner un KMEANS sur l'équité avec nombre de centres de gravité qu'on veut
     * @return les combos groupés par clusters
     */
    private List<ClusterKMeans<ComboEquite>> clusteriserRangeParEquite() {
        List<ComboEquite> combosParEquite = new ArrayList<>();
        // on insère les combos dans un objet clusterisable
        for (NoeudEquilibrage noeudEquilibrage : noeudEquilibrages) {
            ComboEquite comboEquite = new ComboEquite(noeudEquilibrage);
            combosParEquite.add(comboEquite);
        }

        ClusteringKMeans<ComboEquite> kMeans = new ClusteringKMeans<>();
        kMeans.initialiser(combosParEquite);
        kMeans.ajusterClusters(nCentresGravite);

        return kMeans.getClusters();
    }

    public List<ClusterEquilibrage> getResultats() {
        return null;
    }

    // extension des clusters


}
