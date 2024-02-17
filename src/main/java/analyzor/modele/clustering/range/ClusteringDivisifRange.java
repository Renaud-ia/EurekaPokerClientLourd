package analyzor.modele.clustering.range;

import analyzor.modele.clustering.cluster.ClusterDeBase;
import analyzor.modele.clustering.objets.ComboPostClustering;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

import java.util.ArrayList;
import java.util.List;

/**
 * interface avec le reste du projet
 * trouve la meilleure division de range lié à l'équite et aux observations
 * construit l'ACP, appelle l'optimiseur d'hypothèse
 * nettoie les clusters et les renvoie
 */
public class ClusteringDivisifRange {
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
        List<ClusterDeBase<ComboPreClustering>> meilleureHypothese = optimiseurHypothese.meilleureHypothese();

        return nettoyerClusters(meilleureHypothese);
    }

    private List<ClusterEquilibrage> nettoyerClusters(List<ClusterDeBase<ComboPreClustering>> meilleureHypothese) {
        // todo !!!! nettoyer les clusters des intrus et les noter
        List<ClusterEquilibrage> clustersFinaux = new ArrayList<>();
        for (ClusterDeBase<ComboPreClustering> cluster : meilleureHypothese) {
            List<NoeudEquilibrage> noeudsCluster = new ArrayList<>();
            for (ComboPreClustering comboPreClustering : cluster.getObjets()) {
                noeudsCluster.add(comboPreClustering.getNoeudEquilibrage());
            }
            ClusterEquilibrage clusterEquilibrage = new ClusterEquilibrage(noeudsCluster);
            clustersFinaux.add(clusterEquilibrage);
        }

        return clustersFinaux;
    }
}
