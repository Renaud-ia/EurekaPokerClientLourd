package analyzor.modele.clustering.range;

import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;

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
        optimiseurHypothese = new OptimiseurHypothese();
    }
    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        // on met les objets dans leur objet spécial

        // on crée l'ACP

        // on dit à l'optimiseur de créer les hypothèses
        optimiseurHypothese.creerHypotheses(null);
    }

    public List<ClusterEquilibrage> getResultats() {
        List<List<ComboPreClustering>> clustersFinaux = optimiseurHypothese.meilleureHypothese();
        return null;
    }
}
