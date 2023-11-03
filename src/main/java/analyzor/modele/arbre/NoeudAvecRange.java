package analyzor.modele.arbre;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.Entree;
import analyzor.modele.poker.RangeDenombrable;
import analyzor.modele.poker.RangeReelle;

import java.util.List;

public class NoeudAvecRange {
    private List<Entree> entreesCorrespondantes;
    private RangeDenombrable rangeHero;
    private List<RangeReelle> rangesVillains;
    private Action[] arbreActions;
    private int[] observations;

    public NoeudAvecRange(RangeDenombrable range) {
        this.rangeHero = range;
    }

    public void ajouterEntree(Entree nouvelleEntree) {
        entreesCorrespondantes.add(nouvelleEntree);
    }
}
