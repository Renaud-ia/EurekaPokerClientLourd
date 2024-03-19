package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalculatriceEquite {
    protected HashMap<Integer, Float> pctRangeHero;
    protected HashMap<Integer, Float> pctRangeVillain;
    private final int nPercentiles;
    private final Evaluator evaluateur = new Evaluator();
    private final BoardRandomizer boardRandomizer;
    public CalculatriceEquite(ConfigCalculatrice configCalculatrice) {
        this.pctRangeHero = configCalculatrice.pctRangeHero;
        this.pctRangeVillain = configCalculatrice.pctRangeVillain;
        this.nPercentiles = configCalculatrice.nPercentiles;
        boardRandomizer = new BoardRandomizer(configCalculatrice.nSimus);
    }

    private float equiteMainBoard(ComboReel comboHero, Board board, List<RangeReelle> rangesVillains) {
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
            RangeReelle rangeCopiee = range.copie();
            retirerCartes(comboHero.getCartes(), rangeCopiee);
            retirerCartes(board.getCartes(), rangeCopiee);
            List<ComboReel> echantillon = rangeCopiee.obtenirEchantillon(tailleEchantillon, pctRangeVillain.get(tailleBoard));
            combosVillains.add(echantillon);
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
            else if (heroRank == minVillainRank) equite += (float) 1 / rangesVillains.size();

        }

        return equite / tailleEchantillon;
    }

    /**
     * Retourne l'équité d'une main vs range
     * flexible sur n'importe quel street (river = simulation exacte), on peut rentrer un board ou non
     */
    public float equiteGlobaleMain(ComboReel comboHero, Board board, List<RangeReelle> rangesVillains) {
        int sizeRiver = 5;
        List<Board> randomRivers = randomBoards(comboHero, board, sizeRiver);
        float sum = 0;
        for (Board river : randomRivers) {
            sum += equiteMainBoard(comboHero, river, rangesVillains);
        }
        return sum / randomRivers.size();
    }

    public EquiteFuture equiteFutureMain(ComboReel comboReel, Board board, List<RangeReelle> rangesVillains) {
        EquiteFuture equiteFuture = new EquiteFuture(nPercentiles);
        List<List<Board>> randomStreets = prochainesStreets(comboReel, board);

        for (List<Board> street : randomStreets) {
            float[] resultats = new float[street.size()];

            int index = 0;
            for (Board boardTeste : street) {
                resultats[index++] = equiteGlobaleMain(comboReel, boardTeste, rangesVillains);
            }
            equiteFuture.ajouterResultatStreet(resultats);
        }

        return equiteFuture;
    }

    public MatriceEquite equiteRange(RangeReelle rangeHero, Board board, List<RangeReelle> rangesVillains) {
        float pctRange = pctRangeHero.get(board.taille());
        int nEchantillon = (int) (pctRange * rangeHero.nCombos());
        int sizeRiver = 5;

        // important copier la range pour ne pas modifier range originale
        RangeReelle rangeTest = rangeHero.copie();
        retirerCartes(board.getCartes(), rangeTest);
        List<ComboReel> echantillon = rangeTest.obtenirEchantillon(nEchantillon, pctRange);

        int longueurMatrice = echantillon.size();
        MatriceEquite matrice = new MatriceEquite(nPercentiles, longueurMatrice);

        for (ComboReel comboTeste : echantillon) {
            List<Board> randomRivers = randomBoards(comboTeste, board, sizeRiver);
            float[] resultats = new float[randomRivers.size()];

            int index = 0;
            for (Board boardTeste : randomRivers) {
                resultats[index++] = equiteGlobaleMain(comboTeste, boardTeste, rangesVillains);
            }

            matrice.ajouterResultatsRiver(resultats);
        }
        matrice.remplissageFini();
        return matrice;
    }

    private List<Board> randomBoards(ComboReel comboHero, Board board, int sizeBoard) {
        retirerCartes(comboHero.getCartes());
        retirerCartes(board.getCartes());

        List<Board> randomBoards = boardRandomizer.obtenirEchantillon(board, sizeBoard);

        ajouterCartes(comboHero.getCartes());
        ajouterCartes(board.getCartes());
        return randomBoards;
    }

    private List<List<Board>> prochainesStreets(ComboReel comboReel, Board board) {
        List<List<Board>> prochainesStreets = new ArrayList<>();

        if (board.taille() == 0) {
            prochainesStreets.add(randomBoards(comboReel, board, 3));
        }

        if (board.taille() < 4) {
            prochainesStreets.add(randomBoards(comboReel, board, 4));
        }

        if (board.taille() < 5) {
            prochainesStreets.add(randomBoards(comboReel, board, 5));
        }

        return prochainesStreets;
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


    public static void main(String[] args) {
        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
        CalculatriceEquite calculatriceEquite = new CalculatriceEquite(configCalculatrice);

        ComboReel comboHero;

        Board board = new Board("AcKcQc");

        List<RangeReelle> rangesVillains = new ArrayList<>();
        GenerateurRange generateurRange = new GenerateurRange();
        RangeReelle rangeVillain = generateurRange.bottomRange(0.7f);
        rangesVillains.add(rangeVillain);

        comboHero = new ComboReel('A', 'h', 'T', 'd');
        EquiteFuture equiteFuture = calculatriceEquite.equiteFutureMain(comboHero, board, rangesVillains);
        System.out.println(equiteFuture);
        comboHero = new ComboReel('K', 's', 'Q', 's');
        equiteFuture = calculatriceEquite.equiteFutureMain(comboHero, board, rangesVillains);
        System.out.println(equiteFuture);
        comboHero = new ComboReel('K', 'h', 'T', 'h');
        equiteFuture = calculatriceEquite.equiteFutureMain(comboHero, board, rangesVillains);
        System.out.println(equiteFuture);
        long startTime = System.nanoTime();
        comboHero = new ComboReel('T', 'h', '9', 'h');
        equiteFuture = calculatriceEquite.equiteFutureMain(comboHero, board, rangesVillains);
        System.out.println(equiteFuture);
        long endTime = System.nanoTime();

        comboHero = new ComboReel('6', 'h', '6', 's');
        equiteFuture = calculatriceEquite.equiteFutureMain(comboHero, board, rangesVillains);
        System.out.println(equiteFuture);


        RangeReelle rangeHero = new RangeReelle();
        rangeHero.remplir();
        MatriceEquite matriceEquite = calculatriceEquite.equiteRange(rangeHero, board, rangesVillains);
        System.out.println(matriceEquite);

        double dureeMS = (endTime - startTime) / 1_000_000.0;
        System.out.println(dureeMS);

    }
}
