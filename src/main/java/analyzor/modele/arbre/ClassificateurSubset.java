package analyzor.modele.arbre;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Situation;
import analyzor.modele.parties.SituationIso;

import java.util.ArrayList;
import java.util.List;

public class ClassificateurSubset extends Classificateur {
    @Override
    public List<SituationIsoAvecRange> obtenirSituations(List<Entree> entreesSituation) {
        if (!super.situationValide(entreesSituation)) return new ArrayList<>();
        List<SituationIsoAvecRange> situationsDuRang = new ArrayList<>();
        //todo

        List<List<Entree>> clustersSubsets = clusteriserLeafEtSubset(entreesSituation);

        for(List<Entree> cluster : clustersSubsets) {
            List<List<Entree>> clustersActions = clusteriserActions(cluster);

            for (List<Entree> clusterFinal : clustersActions) {
                //on attribue ISO CODE, on le garde pour le retourner + la range
            }
        }
        return situationsDuRang;
    }

    private List<List<Entree>> clusteriserLeafEtSubset(List<Entree> entreesSituation) {
        //todo
        return new ArrayList<>(new ArrayList<>());
    }
}
