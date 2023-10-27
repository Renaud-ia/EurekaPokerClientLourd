package analyzor.modele.arbre;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.GestionnaireFormat;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Situation;

import java.util.ArrayList;
import java.util.List;

public abstract class Classificateur implements CreerLabel, RetrouverLabel {

    List<List<Entree>> clusteriserSRPB(List<Entree> entrees) {
        //todo
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
}
