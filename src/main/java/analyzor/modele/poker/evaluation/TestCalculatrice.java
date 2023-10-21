package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.poker.RangeReelle;

import java.util.ArrayList;
import java.util.List;

public class TestCalculatrice {
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
