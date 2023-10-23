package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculatriceEquite {
    protected HashMap<Integer, Float> pctRangeHero;
    protected HashMap<Integer, Float> pctRangeVillain;
    private final int nPercentiles;
    private final Evaluator evaluateur = new Evaluator();
    private BoardRandomizer boardRandomizer;
    public CalculatriceEquite(ConfigCalculatrice configCalculatrice) {
        this.pctRangeHero = configCalculatrice.pctRangeHero;
        this.pctRangeVillain = configCalculatrice.pctRangeVillain;
        this.nPercentiles = configCalculatrice.nPercentiles;
        boardRandomizer = new BoardRandomizer(configCalculatrice.nSimus);
    }

    private float equiteMainBoard(ComboReel comboHero, Board board, List<RangeReelle> rangesVillains) {
        long startTime = 0;
        long endTime = 0;
        int nombreVillains = rangesVillains.size();
        int tailleBoard = board.taille();

        int tailleEchantillon = 0;
        for (RangeReelle range : rangesVillains) {
            int nEchantillon = (int) (pctRangeVillain.get(tailleBoard) * range.nCombos());
            if (nEchantillon > tailleEchantillon) tailleEchantillon = nEchantillon;
        }
        if (tailleEchantillon == 0) throw new IllegalArgumentException("Echantillon nul, avez vous rentré au moins une range?");


        List<List<ComboReel>> combosVillains = new ArrayList<>();

        for (RangeReelle range : rangesVillains) {
            startTime = System.nanoTime();
            RangeReelle rangeCopiee = range.copie();
            retirerCartes(comboHero.getCartes(), rangeCopiee);
            retirerCartes(board.getCartes(), rangeCopiee);
            List<ComboReel> echantillon = rangeCopiee.obtenirEchantillon(tailleEchantillon, pctRangeVillain.get(tailleBoard));
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
         * flexible sur n'importe quel street (river = simulation exacte), on peut rentrer un board ou non
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
        retirerCartes(comboHero.getCartes());
        retirerCartes(board.getCartes());

        List<Board> randomBoards = boardRandomizer.obtenirEchantillon(board, sizeBoard);

        ajouterCartes(comboHero.getCartes());
        ajouterCartes(board.getCartes());
        return randomBoards;
    }

    private void ajouterCartes(List<Carte> cartes) {
        for (Carte carteAjoutee : cartes) {
            boardRandomizer.ajouterCartes(carteAjoutee);
        }
    }

    private void retirerCartes(List<Carte> cartes, RangeReelle rangeCopiee) {
        for (Carte carteRetiree : cartes) {
            rangeCopiee.retirerCarte(carteRetiree);
        }
    }

    private void retirerCartes(List<Carte> cartes) {
        for (Carte carteRetiree : cartes) {
            boardRandomizer.retirerCarte(carteRetiree);
        }
    }
}
