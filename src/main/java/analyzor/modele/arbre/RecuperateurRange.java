package analyzor.modele.arbre;

import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.TourMain;
import analyzor.modele.poker.RangeDynamique;

// va récupérer les ranges
public class RecuperateurRange {
    // todo c'est quoi cette merde!!
    public RangeDynamique rangeMoyenneFlop(NoeudAbstrait noeudAbstrait) {
        return new RangeDynamique();
    }

    public NoeudAbstrait getNoeudTheorique(Entree entree) {
        return new NoeudAbstrait(0, TourMain.Round.PREFLOP);
    }
}
