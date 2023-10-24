package analyzor.modele.poker.benchmark;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.poker.RangeReelle;

import java.util.ArrayList;
import java.util.List;

public class GenerateurTestsEquite {
    public List<TestEquite> testsEquite = new ArrayList<>();

    public GenerateurTestsEquite() {
        //preflop
        testsEquite.add(new TestEquite("AsAc", "", 1, 0.8523f));
        testsEquite.add(new TestEquite("AsAc", "", 2, 0.7346f));

        testsEquite.add(new TestEquite("AsKs", "", 1, 0.6705f));
        testsEquite.add(new TestEquite("AsKs", "", 2, 0.5081f));

        testsEquite.add(new TestEquite("JsTs", "", 1, 0.5753f));
        testsEquite.add(new TestEquite("JsTs", "", 2, 0.4185f));

        testsEquite.add(new TestEquite("6h6s", "", 1, 0.6317f));
        testsEquite.add(new TestEquite("6h6s", "", 2, 0.4310f));

        //flop
        testsEquite.add(new TestEquite("6h6s", "KhQhJh", 2, 0.3825f));
        testsEquite.add(new TestEquite("Ah6s", "KhQhJh", 2, 0.5307f));
        testsEquite.add(new TestEquite("4h6s", "KhQhJh", 2, 0.2263f));

        //turn
        testsEquite.add(new TestEquite("Ah6s", "KhQhJh5h", 2, 0.9815f));
        testsEquite.add(new TestEquite("Ah6s", "KhQhJhJc", 2, 0.4072f));

        //river
        testsEquite.add(new TestEquite("Ah6s", "KhQhJhJc3h", 2, 0.9419f));
        testsEquite.add(new TestEquite("9h6s", "KhQhJhJc7h", 1, 0.8838f));
    }

    public class TestEquite {
        public ComboReel heroCombo;
        public Board board;
        public List<RangeReelle> rangesVillains = new ArrayList<>();
        public float result;
        public TestEquite(String heroCards, String boardString, int nRangesVillains, float result) {
            this.heroCombo = new ComboReel(heroCards.charAt(0), heroCards.charAt(1), heroCards.charAt(2), heroCards.charAt(3));
            this.board = new Board(boardString);
            for (int i = 0; i < nRangesVillains; i++) {
                RangeReelle rangeReelle = new RangeReelle();
                rangeReelle.remplir();
                rangesVillains.add(rangeReelle);
            }
            this.result = result;
        }
    }
}
