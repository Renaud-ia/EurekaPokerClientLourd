package analyzor.modele.arbre.classificateurs;

import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public class ClassificateurSubset extends Classificateur {
    public ClassificateurSubset(FormatSolution formatSolution) {
        super(formatSolution);

    }

    @Override
    public void creerSituations(List<Entree> entreesSituation) {
        if (super.situationInvalide(entreesSituation).isEmpty()) return ;
        List<NoeudDenombrable> situationsDuRang = new ArrayList<>();
        //todo

        // SI ON A DES SITUATIONS DE RANG2, on a déjà les SituationIso

        List<List<Entree>> clustersSubsets = clusteriserLeafEtSubset(entreesSituation);

    }

    @Override
    public boolean construireCombosDenombrables() {
        return false;
    }

    @Override
    public List<NoeudDenombrable> obtenirSituations() {
        return null;
    }

    private List<List<Entree>> clusteriserLeafEtSubset(List<Entree> entreesSituation) {
        //todo
        return new ArrayList<>(new ArrayList<>());
    }
}
