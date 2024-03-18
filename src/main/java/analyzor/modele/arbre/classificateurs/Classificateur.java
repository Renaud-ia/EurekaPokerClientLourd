package analyzor.modele.arbre.classificateurs;

import analyzor.modele.berkeley.EnregistrementNormalisation;
import analyzor.modele.clustering.HierarchiqueSPRB;
import analyzor.modele.clustering.SpecialBetSize;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.clustering.objets.MinMaxCalculSituation;
import analyzor.modele.estimation.CalculInterrompu;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.ErreurCritique;
import analyzor.modele.parties.Entree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public abstract class Classificateur implements CreerLabel, RetrouverLabel {
    protected final ArbreAbstrait arbreAbstrait;
    protected final FormatSolution formatSolution;
    protected final static int MIN_ECHANTILLON = 50;
    protected final Logger logger = LogManager.getLogger(Classificateur.class);

    protected Classificateur(FormatSolution formatSolution) {
        this.formatSolution = formatSolution;
        this.arbreAbstrait = new ArbreAbstrait(formatSolution);
    }

    /**
     * procédure de clusterisation par stack/pot/bounty
     * enregistre dans berkeley les valeurs de normalisation appliquées
     * @param entrees liste des entrées à clusteriser
     * @param minimumPoints nombre min de points par cluster SPB
     * @return les entrées groupées dans des clusters
     */
    List<ClusterSPRB> clusteriserSPRB(List<Entree> entrees, int minimumPoints) throws CalculInterrompu {
        logger.debug("Lancement du clustering SPRB");
        HierarchiqueSPRB clusteringEntreeMinEffectif = new HierarchiqueSPRB();

        clusteringEntreeMinEffectif.ajouterDonnees(entrees);

        // on le garde en mémoire dans BDD
        MinMaxCalculSituation minMaxCalculSituation = clusteringEntreeMinEffectif.getMinMaxCalcul();

        long idNoeudTheorique =
                arbreAbstrait.noeudPrecedent(new NoeudAbstrait(entrees.getFirst().getIdNoeudTheorique())).toLong();
        EnregistrementNormalisation enregistrementNormalisation = new EnregistrementNormalisation();

        try {
            enregistrementNormalisation.enregistrerMinMax(
                    formatSolution.getId(),
                    idNoeudTheorique,
                    minMaxCalculSituation
            );
        }

        catch (Exception e) {
            throw new ErreurCritique("Impossible de sauvegarder les valeurs normalisées dans la BDD");
        }

        // todo PRODUCTION log sensible à supprimer
        logger.debug("Valeurs minimums enregistrées : " + Arrays.toString(minMaxCalculSituation.getMinValeurs()));
        logger.debug("Valeurs maximums enregistrées : " + Arrays.toString(minMaxCalculSituation.getMaxValeurs()));

        return clusteringEntreeMinEffectif.construireClusters(minimumPoints);
    }

    /**
     * procédure de vérification
     * @param entreesSituation
     * @return
     */
    protected List<Entree> situationInvalide(List<Entree> entreesSituation) {
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
                // todo PRODUCTION log sensible à supprimer
                logger.debug("Noeud action sera traité : " + new NoeudAbstrait(idAction) + ", nombre d'entrées : " + entreesCorrespondantes.size());
            }
            else {
                // todo PRODUCTION log sensible à encrypter
                logger.info("Pas assez d'entrées pour : " + new NoeudAbstrait(idAction) + ", nombre d'entrées : " + entreesCorrespondantes.size());
            }
        }

        // si on a plus de deux actions, on retourne true sinon false
        if (actionsValides < 2) {
            return new ArrayList<>();
        }

        else return entreesAConserver;
    }

    protected List<ClusterBetSize> clusteriserBetSize(List<Entree> entreesAction, int minEffectifBetSize) {
        // todo limiter le nombre de BetSize possible
        int maxBetSize = 4;
        SpecialBetSize algoClustering = new SpecialBetSize(maxBetSize);
        algoClustering.ajouterDonnees(entreesAction);

        return algoClustering.construireClusters(minEffectifBetSize);
    }
}
