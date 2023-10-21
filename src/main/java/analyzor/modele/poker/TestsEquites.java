package analyzor.modele.poker;

import analyzor.modele.poker.evaluation.CalculatriceEquite;

import java.util.ArrayList;
import java.util.List;

public class TestsEquites {
    public static void main(String[] args) {
    CalculatriceEquite calculatrice = new CalculatriceEquite();
    ComboReel heroCombo = new ComboReel('A', 'c', 'K', 'c');
    Board board = new Board();
    List<RangeReelle> rangesVillains = new ArrayList<>();
    rangesVillains.add(new RangeReelle());
    float equite = calculatrice.equiteGlobaleMain(heroCombo, board, rangesVillains);
    System.out.println(equite);
    }
}
