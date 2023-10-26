package analyzor.modele.arbre;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.GestionnaireFormat;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Situation;

import java.util.ArrayList;
import java.util.List;

public abstract class Classificateur implements CreerLabel, RetrouverLabel {
    List<Entree> obtenirLesEntrees(Situation situation, FormatSolution formatSolution) {
        List<Entree> toutesLesEntrees = GestionnaireFormat.getEntrees(formatSolution);
        //todo récupérer par situation
        return new ArrayList<>();
    }

    List<List<Entree>> clusteriserSRPB(List<Entree> entrees) {
        //todo
        return new ArrayList<>();
    }

    List<List<Entree>> clusteriserActions(List<Entree> cluster) {
        //todo
        return new ArrayList<>();
    }
}
