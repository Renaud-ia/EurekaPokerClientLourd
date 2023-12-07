package analyzor.modele.arbre.classificateurs;

import analyzor.modele.clustering.HierarchiqueSPRB;
import analyzor.modele.clustering.KMeansBetSize;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.parties.Entree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class Classificateur implements CreerLabel, RetrouverLabel {
    protected final Logger logger = LogManager.getLogger(Classificateur.class);

    List<ClusterSPRB> clusteriserSPRB(List<Entree> entrees, int minimumPoints) {
        HierarchiqueSPRB clusteringEntreeMinEffectif = new HierarchiqueSPRB();
        clusteringEntreeMinEffectif.ajouterDonnees(entrees);

        return clusteringEntreeMinEffectif.construireClusters(minimumPoints);
    }

    /**
     * procédure de vérification
     * @param entreesSituation
     * @return
     */
    protected boolean situationValide(List<Entree> entreesSituation) {
        //todo ajouter un nombre minimum de mains
        if (entreesSituation.isEmpty()) return false;
        else return true;
    }

    protected List<ClusterBetSize> clusteriserBetSize(List<Entree> entreesAction, int minEffectifBetSize) {
        // todo limiter le nombre de BetSize possible
        int maxBetSize = 4;
        KMeansBetSize algoClustering = new KMeansBetSize(maxBetSize);
        algoClustering.ajouterDonnees(entreesAction);

        return algoClustering.construireClusters(minEffectifBetSize);
    }
}
