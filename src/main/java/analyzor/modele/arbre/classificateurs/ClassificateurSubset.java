package analyzor.modele.arbre.classificateurs;

import analyzor.modele.arbre.NoeudDenombrable;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public class ClassificateurSubset extends Classificateur {
    @Override
    public List<NoeudDenombrable> obtenirSituations(List<Entree> entreesSituation, FormatSolution formatSolution) {
        if (!super.situationValide(entreesSituation)) return new ArrayList<>();
        List<NoeudDenombrable> situationsDuRang = new ArrayList<>();
        //todo

        // SI ON A DES SITUATIONS DE RANG2, on a déjà les SituationIso

        List<List<Entree>> clustersSubsets = clusteriserLeafEtSubset(entreesSituation);

        return situationsDuRang;
    }

    private List<List<Entree>> clusteriserLeafEtSubset(List<Entree> entreesSituation) {
        //todo
        return new ArrayList<>(new ArrayList<>());
    }
}
