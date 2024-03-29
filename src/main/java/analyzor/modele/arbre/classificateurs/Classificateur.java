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

    protected Classificateur(FormatSolution formatSolution) {
        this.formatSolution = formatSolution;
        this.arbreAbstrait = new ArbreAbstrait(formatSolution);
    }

    
    List<ClusterSPRB> clusteriserSPRB(List<Entree> entrees, int minimumPoints) throws CalculInterrompu {
        HierarchiqueSPRB clusteringEntreeMinEffectif = new HierarchiqueSPRB();

        clusteringEntreeMinEffectif.ajouterDonnees(entrees);

        
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
            throw new ErreurCritique("EA1");
        }

        return clusteringEntreeMinEffectif.construireClusters(minimumPoints);
    }

    
    protected List<Entree> situationInvalide(List<Entree> entreesSituation) {
        
        
        Map<Long, List<Entree>> entreesMap = new HashMap<>();

        for (Entree entree : entreesSituation) {
            long idNoeud = entree.getIdNoeudTheorique();
            entreesMap.computeIfAbsent(idNoeud, k -> new ArrayList<>()).add(entree);
        }

        int actionsValides = 0;
        
        List<Entree> entreesAConserver = new ArrayList<>();
        for (Long idAction : entreesMap.keySet()) {
            List<Entree> entreesCorrespondantes = entreesMap.get(idAction);
            if (entreesCorrespondantes.size() >= MIN_ECHANTILLON) {
                entreesAConserver.addAll(entreesCorrespondantes);
                actionsValides++;
            }
        }

        
        if (actionsValides < 2) {
            return new ArrayList<>();
        }

        else return entreesAConserver;
    }

    protected List<ClusterBetSize> clusteriserBetSize(List<Entree> entreesAction, int minEffectifBetSize) {
        
        int maxBetSize = 4;
        SpecialBetSize algoClustering = new SpecialBetSize(maxBetSize);
        algoClustering.ajouterDonnees(entreesAction);

        return algoClustering.construireClusters(minEffectifBetSize);
    }
}
