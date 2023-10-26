package analyzor.modele.arbre;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Situation;
import analyzor.modele.parties.SituationIso;

import java.util.ArrayList;
import java.util.List;

public class ClassificateurSubset extends Classificateur {
    @Override
    public List<SituationIsoAvecRange> obtenirSituations(Situation situation, FormatSolution formatSolution) {
        //todo
        List<Entree> entreesSituation = this.obtenirLesEntrees(situation, formatSolution);
        List<List<Entree>> clustersSubsets = clusteriserLeafEtSubset(entreesSituation);

        for(List<Entree> cluster : clustersSubsets) {
            List<List<Entree>> clustersActions = clusteriserActions(cluster);

            for (List<Entree> clusterFinal : clustersActions) {
                //on attribue ISO CODE, on le garde pour le retourner + la range
            }
        }
        return new ArrayList<>();
    }

    private List<List<Entree>> clusteriserLeafEtSubset(List<Entree> entreesSituation) {
        //todo
        return new ArrayList<>(new ArrayList<>());
    }
}
