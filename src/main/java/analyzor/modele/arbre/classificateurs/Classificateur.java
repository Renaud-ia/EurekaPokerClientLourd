package analyzor.modele.arbre.classificateurs;

import analyzor.modele.clustering.HierarchiqueSPRB;
import analyzor.modele.clustering.KMeansBetSize;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Classificateur implements CreerLabel, RetrouverLabel {
    protected final static int MIN_ECHANTILLON = 100;
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
        // on va ne garder que les actions qui ont MIN_ECHANTILLON
        // d'abord on crée une hashmap avec les différentes actions
        Map<Long, List<Entree>> entreesMap = new HashMap<>();

        for (Entree entree : entreesSituation) {
            long idNoeud = entree.getIdNoeudTheorique();
            entreesMap.computeIfAbsent(idNoeud, k -> new ArrayList<>()).add(entree);
        }

        int actionsValides = 0;
        // on supprime les actions qui n'ont pas assez d'occurrences
        List<Entree> entreesAConserver = new ArrayList<>();
        for (Long idAction : entreesMap.keySet()) {
            List<Entree> entreesCorrespondantes = entreesMap.get(idAction);
            if (entreesCorrespondantes.size() >= MIN_ECHANTILLON) {
                entreesAConserver.addAll(entreesCorrespondantes);
                actionsValides++;
            }
            else {
                logger.warn("Pas assez d'entrées pour : " + new NoeudAbstrait(idAction));
            }
        }

        // on met à jour la liste d'entreesSituation
        entreesSituation.retainAll(entreesAConserver);

        // si on a plus de deux actions, on retourne true sinon false
        return actionsValides >= 2;
    }

    protected List<ClusterBetSize> clusteriserBetSize(List<Entree> entreesAction, int minEffectifBetSize) {
        // todo limiter le nombre de BetSize possible
        int maxBetSize = 4;
        KMeansBetSize algoClustering = new KMeansBetSize(maxBetSize);
        algoClustering.ajouterDonnees(entreesAction);

        return algoClustering.construireClusters(minEffectifBetSize);
    }
}
