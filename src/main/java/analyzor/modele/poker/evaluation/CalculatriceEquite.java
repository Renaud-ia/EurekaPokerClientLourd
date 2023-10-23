package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculatriceEquite {
    private final Map<Integer, Integer> nSimus = new HashMap<>();
    private final float pctRangeHero;
    private final float pctRangeVillain;
    private final int nPercentiles;
    private final Evaluator evaluateur = new Evaluator();
    public CalculatriceEquite(
            int nSimuFlop,
            int nSimuTurn,
            int nSimuRiver,
            float pctRangeHero,
            float pctRangeVillain,
            int nPercentiles
    ) {
        nSimus.put(3, nSimuFlop);
        nSimus.put(4, nSimuTurn);
        nSimus.put(5, nSimuRiver);

        this.pctRangeHero = pctRangeHero;
        this.pctRangeVillain = pctRangeVillain;
        this.nPercentiles = nPercentiles;
    }

    public CalculatriceEquite() {
        nSimus.put(3, 10000);
        nSimus.put(4, 10000);
        nSimus.put(5, 10000);

        this.pctRangeHero = 0.2f;
        this.pctRangeVillain = 0.2f;
        this.nPercentiles = 5;
    }

    private float equiteMainBoard(ComboReel comboHero, Board board, List<RangeReelle> rangesVillains) {
        long startTime = 0;
        long endTime = 0;
        int nombreVillains = rangesVillains.size();

        int tailleEchantillon = 0;
        for (RangeReelle range : rangesVillains) {
            int nEchantillon = (int) (pctRangeVillain * range.nCombos());
            if (nEchantillon > tailleEchantillon) tailleEchantillon = nEchantillon;
        }
        if (tailleEchantillon == 0) throw new IllegalArgumentException("Echantillon nul, avez vous rentré au moins une range?");


        List<List<ComboReel>> combosVillains = new ArrayList<>();

        for (RangeReelle range : rangesVillains) {
            startTime = System.nanoTime();
            RangeReelle rangeCopiee = range.copie();
            retirerCartes(comboHero.getCartes(), rangeCopiee);
            retirerCartes(board.getCartes(), rangeCopiee);
            List<ComboReel> echantillon = rangeCopiee.obtenirEchantillon(tailleEchantillon, pctRangeVillain);
            combosVillains.add(echantillon);
            endTime = System.nanoTime();
        }

        int heroRank = evaluateur.evaluate(comboHero, board);
        float equite = 0;
        for (int i = 0; i < tailleEchantillon; i++) {
            int minVillainRank = LookupTable.MAX_HIGH_CARD;
            for (int indexVillain = 0; indexVillain < nombreVillains; indexVillain++) {
                ComboReel comboVillain = combosVillains.get(indexVillain).get(i);
                int villainRank = evaluateur.evaluate(comboVillain, board);
                if (villainRank < minVillainRank) minVillainRank = villainRank;
            }
            if (heroRank < minVillainRank) equite += 1;
            else if (heroRank == minVillainRank) equite += 0.5;

        }
        double dureeMS = (endTime - startTime) / 1_000_000.0;
        //System.out.println("Benchmark (en ms) : " + dureeMS);
        return equite / tailleEchantillon;
    }

    public float equiteGlobaleMain(ComboReel comboHero, Board board, List<RangeReelle> rangesVillains) {
        /**
         * Retourne l'équité d'une main vs range
         * flexible sur n'importe quel street, on peut rentrer un board ou non
         */
        int sizeRiver = 5;
        List<Board> randomRivers = randomBoards(comboHero, board, sizeRiver);
        float sum = 0;
        for (Board river : randomRivers) {
            sum += equiteMainBoard(comboHero, river, rangesVillains);
        }
        return sum / randomRivers.size();
    }

    private List<Board> randomBoards(ComboReel comboHero, Board board, int sizeBoard) {
        Deck deck = new Deck();
        retirerCartes(comboHero.getCartes(), deck);
        retirerCartes(board.getCartes(), deck);

        int nSimus = this.nSimus.get(sizeBoard);

        return deck.obtenirEchantillon(board, sizeBoard, nSimus);
    }

    private void retirerCartes(List<Carte> cartes, RangeReelle rangeCopiee) {
        for (Carte carteRetiree : cartes) {
            rangeCopiee.retirerCarte(carteRetiree);
        }
    }

    private void retirerCartes(List<Carte> cartes, Deck deck) {
        for (Carte carteRetiree : cartes) {
            deck.retirerCarte(carteRetiree);
        }
    }
}
