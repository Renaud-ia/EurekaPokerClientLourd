package analyzor;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;

import java.util.List;

public class TestArbre {
    public static void main(String[] args) {
        FormatSolution formatSolution = new FormatSolution("", Variante.PokerFormat.MTT, 0, 0, 0, 0, false, 6, 0, 0);
        TourMain.Round round = TourMain.Round.PREFLOP;
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        System.out.println("Nombre de situations : " + arbreAbstrait.obtenirNoeudsGroupes(round).size());
        for (NoeudAbstrait noeudSituation : arbreAbstrait.obtenirNoeudsGroupes(round).keySet()) {
            System.out.println(noeudSituation);
        }
    }
}
