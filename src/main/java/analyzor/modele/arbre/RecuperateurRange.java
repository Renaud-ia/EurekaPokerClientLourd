package analyzor.modele.arbre;

import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.TourMain;
import analyzor.modele.poker.RangeDynamique;

// va récupérer les ranges
public class RecuperateurRange {

    public NoeudAbstrait getNoeudTheorique(Entree entree) {
        return new NoeudAbstrait(0, TourMain.Round.PREFLOP);
    }
}
