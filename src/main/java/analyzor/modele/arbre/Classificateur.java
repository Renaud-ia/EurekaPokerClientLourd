package analyzor.modele.arbre;

import analyzor.modele.clustering.ClusteringHierarchique;
import analyzor.modele.clustering.ClusteringHierarchiqueSPRB;
import analyzor.modele.parties.Entree;
import analyzor.modele.poker.RangeDenombrable;
import analyzor.modele.poker.RangeIso;

import java.util.ArrayList;
import java.util.List;

import static analyzor.modele.config.ValeursConfig.effectifMinClusterSRPB;

public abstract class Classificateur implements CreerLabel, RetrouverLabel {

    /**
     * utilisé par ClassificateurCumulatif et ClassificateurDynamique
     * @param entrees la liste des entrées à clusteriser selon SPRB
     * @return des sous listes groupées par SPRB, si pas possible null
     */
    //
    List<List<Entree>> clusteriserSRPB(List<Entree> entrees) {
        ClusteringHierarchiqueSPRB clusteringSRPB = new ClusteringHierarchiqueSPRB(ClusteringHierarchique.MethodeLiaison.CENTREE);
        clusteringSRPB.ajouterDonnees(entrees);
        int minimumCluster = effectifMinClusterSRPB;

        // todo seconde procédure pour regrouper les valeurs??

        return clusteringSRPB.construireClusters(minimumCluster);
    }

    List<List<Entree>> clusteriserActions(List<Entree> cluster) {
        //todo
        return new ArrayList<>();
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

    // on va simplement récupérer la range absolue de l'entre précédente (couple SituationIso/ActionIso)
    // on a besoin d'un accès BDD
    protected RangeDenombrable recupererRange(Entree entree) {
        //todo
        return new RangeIso();
    }
}
