package analyzor.modele.poker;

import analyzor.modele.poker.evaluation.CalculatriceEquite;

import java.util.ArrayList;
import java.util.List;

public class TestsEquites {
    public static void main(String[] args) {
    CalculatriceEquite calculatrice = new CalculatriceEquite();
    ComboReel heroCombo = new ComboReel('4', 's', 'K', 'c');
    List<Carte> cartesBoard = new ArrayList<>();
    cartesBoard.add(new Carte('A', 's'));
    cartesBoard.add(new Carte('K', 's'));
    cartesBoard.add(new Carte('Q', 's'));
    cartesBoard.add(new Carte('5', 's'));
    cartesBoard.add(new Carte('3', 's'));
    Board board = new Board(cartesBoard);
    List<RangeReelle> rangesVillains = new ArrayList<>();
    rangesVillains.add(new RangeReelle());
    rangesVillains.add(new RangeReelle());
    long startTime = System.nanoTime();
    float equite = calculatrice.equiteGlobaleMain(heroCombo, board, rangesVillains);
    long endTime = System.nanoTime();
    double dureeMS = (endTime - startTime) / 1_000_000.0;
    System.out.println(equite);
    System.out.println("Temps de calcul de l'équité (en ms) : " + dureeMS);

    }
}
