package analyzor.modele.arbre;

import analyzor.modele.clustering.ClusteringHierarchique;
import analyzor.modele.clustering.ClusteringSRPB;
import analyzor.modele.parties.Entree;
import analyzor.modele.poker.RangeDenombrable;
import analyzor.modele.poker.RangeIso;

import java.util.ArrayList;
import java.util.List;

public abstract class Classificateur implements CreerLabel, RetrouverLabel {

    // utilisé par ClassificateurCumulatif et ClassificateurDynamique
    List<List<Entree>> clusteriserSRPB(List<Entree> entrees) {
        ClusteringSRPB clusteringSRPB = new ClusteringSRPB(ClusteringHierarchique.MethodeLiaison.CENTREE);
        clusteringSRPB.ajouterDonnees(entrees);
        int minimumCluster = 100;
        List<List<Entree>> entreesClusterisees = clusteringSRPB.construireClusters(minimumCluster);
        return new ArrayList<>();
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
