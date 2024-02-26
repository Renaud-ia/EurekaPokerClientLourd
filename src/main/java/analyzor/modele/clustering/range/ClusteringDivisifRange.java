package analyzor.modele.clustering.range;

import analyzor.modele.clustering.cluster.ClusterDeBase;
import analyzor.modele.clustering.objets.ComboPostClustering;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * interface avec le reste du projet
 * trouve la meilleure division de range lié à l'équite et aux observations
 * construit l'ACP, appelle l'optimiseur d'hypothèse
 * nettoie les clusters et les renvoie
 */
public class ClusteringDivisifRange {
    private final static Logger logger = LogManager.getLogger(ClusteringDivisifRange.class);
    private final OptimiseurHypothese optimiseurHypothese;
    public ClusteringDivisifRange(int nSituations) {
        optimiseurHypothese = new OptimiseurHypothese(nSituations);
    }
    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
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
        List<ComboPreClustering> meilleureHypothese = optimiseurHypothese.meilleureHypothese();

        logger.debug("Clustering terminé");

        return etendreLesCentres(meilleureHypothese);
    }

    /**
     * va étendre les centres de gravité
     * @param centresGravite combos qui cosntituent les centres de gravité de la range
     * @return des clusters formés et prêts à être équilibrés
     */
    private List<ClusterEquilibrage> etendreLesCentres(List<ComboPreClustering> centresGravite) {
        return null;
    }

    private List<ClusterEquilibrage> nettoyerClusters(List<ClusterDeBase<ComboPreClustering>> meilleureHypothese) {
        // todo !!!! nettoyer les clusters des intrus et les noter

        List<ClusterEquilibrage> clustersFinaux = new ArrayList<>();
        for (ClusterDeBase<ComboPreClustering> cluster : meilleureHypothese) {
            logger.trace("Cluster FORME");
            List<NoeudEquilibrage> noeudsCluster = new ArrayList<>();
            for (ComboPreClustering comboPreClustering : cluster.getObjets()) {
                noeudsCluster.add(comboPreClustering.getNoeudEquilibrage());
                logger.trace(comboPreClustering.getNoeudEquilibrage());
            }
            ClusterEquilibrage clusterEquilibrage = new ClusterEquilibrage(noeudsCluster);
            clustersFinaux.add(clusterEquilibrage);
        }

        return clustersFinaux;
    }
}
