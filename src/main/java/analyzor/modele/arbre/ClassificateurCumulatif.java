package analyzor.modele.arbre;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.GenerateurSituation;
import analyzor.modele.estimation.GestionnaireFormat;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Situation;
import analyzor.modele.parties.SituationIso;

import java.util.ArrayList;
import java.util.List;

public class ClassificateurCumulatif extends Classificateur{
    //todo

    @Override
    public List<SituationIsoAvecRange> obtenirSituations(Situation situation, FormatSolution formatSolution) {
        //todo
        List<Entree> entreesSituation = this.obtenirLesEntrees(situation, formatSolution);
        // s'il y a des actions de rang n+1, on va les labelliser
        if (!(GenerateurSituation.estLeaf(situation))) {
            this.labelliserProchainesActions(entreesSituation);
        }

        //dans tous les cas on retourne les situations IsoAvecRange
        List<SituationIsoAvecRange> situationsDuRang = new ArrayList<>();
        //todo

        return situationsDuRang;
    }

    private void labelliserProchainesActions(List<Entree> entreesSituation) {
        List<List<Entree>> clustersSRPB = clusteriserSRPB(entreesSituation);

        for (List<Entree> cluster : clustersSRPB) {
            List<List<Entree>> clustersAction = clusteriserActions(cluster);

            for (List<Entree> clusterFinal : clustersAction) {
                // on labellise les Entrées de rang +1
            }
        }
    }


}
