package analyzor.modele.arbre;

import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Situation;
import analyzor.modele.parties.SituationIso;
import analyzor.modele.poker.RangeDenombrable;

import java.util.List;

public class SituationIsoAvecRange {
    private List<Entree> entreesCorrespondantes;
    private RangeDenombrable range;

    public SituationIsoAvecRange(RangeDenombrable range) {
        this.range = range;
    }

    public void ajouterEntree(Entree nouvelleEntree) {
        entreesCorrespondantes.add(nouvelleEntree);
    }
}
