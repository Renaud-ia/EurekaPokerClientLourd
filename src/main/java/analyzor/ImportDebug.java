package analyzor;

import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;

public class ImportDebug {
    public static void main(String[] args) {
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(3, TourMain.Round.PREFLOP);
        noeudAbstrait.ajouterAction(Move.ALL_IN);
        System.out.println(noeudAbstrait.toLong());
    }
}
