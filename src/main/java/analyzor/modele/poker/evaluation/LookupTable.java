package analyzor.modele.poker.evaluation;

import analyzor.modele.utils.Combinations;
import analyzor.modele.poker.Carte;

import java.util.*;

class LookupTable {
    //TODO OPTIMISATION : initialiser statiquement la LookupTable
    private static final int MAX_STRAIGHT_FLUSH = 10;
    private static final int MAX_FOUR_OF_A_KIND = 166;
    private static final int MAX_FULL_HOUSE = 322;
    private static final int MAX_FLUSH = 1599;
    private static final int MAX_STRAIGHT = 1609;
    private static final int MAX_THREE_OF_A_KIND = 2467;
    private static final int MAX_TWO_PAIR = 3325;
    private static final int MAX_PAIR = 6185;
    static final int MAX_HIGH_CARD = 7462;

    private static final Map<Integer, Integer> MAX_TO_RANK_CLASS = new HashMap<>();
    private static final Map<Integer, String> RANK_CLASS_TO_STRING = new HashMap<>();
    final Map<Long, Integer> flushLookup = new HashMap<>();
    final Map<Long, Integer> unsuitedLookup = new HashMap<>();

    static {
        MAX_TO_RANK_CLASS.put(MAX_STRAIGHT_FLUSH, 1);
        MAX_TO_RANK_CLASS.put(MAX_FOUR_OF_A_KIND, 2);
        MAX_TO_RANK_CLASS.put(MAX_FULL_HOUSE, 3);
        MAX_TO_RANK_CLASS.put(MAX_FLUSH, 4);
        MAX_TO_RANK_CLASS.put(MAX_STRAIGHT, 5);
        MAX_TO_RANK_CLASS.put(MAX_THREE_OF_A_KIND, 6);
        MAX_TO_RANK_CLASS.put(MAX_TWO_PAIR, 7);
        MAX_TO_RANK_CLASS.put(MAX_PAIR, 8);
        MAX_TO_RANK_CLASS.put(MAX_HIGH_CARD, 9);

        RANK_CLASS_TO_STRING.put(1, "Straight Flush");
        RANK_CLASS_TO_STRING.put(2, "Four of a Kind");
        RANK_CLASS_TO_STRING.put(3, "Full House");
        RANK_CLASS_TO_STRING.put(4, "Flush");
        RANK_CLASS_TO_STRING.put(5, "Straight");
        RANK_CLASS_TO_STRING.put(6, "Three of a Kind");
        RANK_CLASS_TO_STRING.put(7, "Two Pair");
        RANK_CLASS_TO_STRING.put(8, "Pair");
        RANK_CLASS_TO_STRING.put(9, "High Card");

    }

    protected LookupTable() {
        this.flushes();
        this.multiples();

    }

    protected void flushes() {
        int[] straightFlushes = {
                7936,  // int('0b1111100000000', 2), # royal flush
                3968,  // int('0b111110000000', 2),
                1984,  // int('0b11111000000', 2),
                992,  // int('0b1111100000', 2),
                496,  // int('0b111110000', 2),
                248,  // int('0b11111000', 2),
                124, // int('0b1111100', 2),
                62,  // int('0b111110', 2),
                31,  // int('0b11111', 2),
                4111, // int('0b1000000001111', 2) # 5 high
        };

        List<Integer> flushes = new ArrayList<>();

        LexicographicallyNextBitSequenceGenerator generator =
                new LexicographicallyNextBitSequenceGenerator(0b11111);

        int f;
        boolean notSF;
        for (int i = 0; i < (1277 + straightFlushes.length - 1); i++) {
            f = generator.next();
            notSF = true;

            for (int sf : straightFlushes) {
                if ((f ^ sf) == 0) {
                    notSF = false;
                    break;
                }
            }
            if (notSF) flushes.add(f);
        }
        Collections.reverse(flushes);

        fillInLookupTable(1, straightFlushes, this.flushLookup);

        int[] flushesArray = flushes.stream().mapToInt(Integer::intValue).toArray();
        fillInLookupTable(MAX_FULL_HOUSE + 1, flushesArray, flushLookup);

        straightsAndHighCards(straightFlushes, flushesArray);
    }

    private void straightsAndHighCards(int[] straights, int[] highcards) {
        fillInLookupTable(MAX_FLUSH + 1, straights, unsuitedLookup);
        fillInLookupTable(MAX_PAIR + 1, highcards, unsuitedLookup);
    }

    private void fillInLookupTable(int rankInit, int[] rankbitsList, Map<Long, Integer> lookupTable) {
        int rank = rankInit;
        for (int rb : rankbitsList) {
            long primeProduct = EvaluationCard.primeProductFromRankbits(rb);
            lookupTable.put(primeProduct, rank++);
        }
    }

    private void multiples() {
        List<Integer> backwardsRanks = new ArrayList<>();
        for(int i = Carte.CHAR_RANK_TO_INT_RANK.size() - 1; i > -1; i--) {
            backwardsRanks.add(i);
        }
        int rank;

        // 1) Carré
        rank = MAX_STRAIGHT_FLUSH + 1;

        for (int i : backwardsRanks) {
            List<Integer> kickers = new ArrayList<>(backwardsRanks);
            kickers.remove(Integer.valueOf(i));

            for (int k : kickers) {
                long product = (long) Math.pow(EvaluationCard.PRIMES[i], 4) * (long) EvaluationCard.PRIMES[k];
                unsuitedLookup.put(product, rank++);
            }
        }

        // 2) Full
        for (int i : backwardsRanks) {
            List<Integer> pairRanks = new ArrayList<>(backwardsRanks);
            pairRanks.remove(Integer.valueOf(i));
            for (int pr : pairRanks) {
                long product =
                        ((long) Math.pow(EvaluationCard.PRIMES[i], 3) * (long) Math.pow(EvaluationCard.PRIMES[pr], 2));
                unsuitedLookup.put(product, rank++);
            }
        }

        // 3) Brelan
        rank = LookupTable.MAX_STRAIGHT + 1;

        for (int r : backwardsRanks) {
            List<Integer> kickers = new ArrayList<>(backwardsRanks);
            kickers.remove(Integer.valueOf(r));

            Combinations<Integer> combinator = new Combinations<>(kickers);

            for (List<Integer> kickerCombination : combinator.getCombinations(2)) {
                int c1 = kickerCombination.get(0);
                int c2 = kickerCombination.get(1);

                long product = (long) Math.pow(EvaluationCard.PRIMES[r], 3)
                        * (long) EvaluationCard.PRIMES[c1] * (long) EvaluationCard.PRIMES[c2];
                unsuitedLookup.put(product, rank++);
            }
        }

        // 4) Deux paires
        rank = LookupTable.MAX_THREE_OF_A_KIND + 1;

        Combinations<Integer> combinator = new Combinations<>(backwardsRanks);
        for (List<Integer> pairCombination : combinator.getCombinations(2)) {
            int pair1 = pairCombination.get(0);
            int pair2 = pairCombination.get(1);

            List<Integer> kickers = new ArrayList<>(backwardsRanks);
            kickers.remove(Integer.valueOf(pair1));
            kickers.remove(Integer.valueOf(pair2));

            for (int kicker : kickers) {
                long product = ((long) Math.pow(EvaluationCard.PRIMES[pair1], 2) * (long) Math.pow(EvaluationCard.PRIMES[pair2], 2)
                                        * (long) EvaluationCard.PRIMES[kicker]);
                unsuitedLookup.put(product, rank++);
            }
        }

        // 5) Paire
        rank = LookupTable.MAX_TWO_PAIR + 1;

        for (int pairRank : backwardsRanks) {
            List<Integer> kickers = new ArrayList<>(backwardsRanks);
            kickers.remove(Integer.valueOf(pairRank));
            Combinations<Integer> kickerCombinator = new Combinations<>(kickers);

            for (List<Integer> kickerCombination: kickerCombinator.getCombinations(3)) {
                int k1 = kickerCombination.get(0);
                int k2 = kickerCombination.get(1);
                int k3 = kickerCombination.get(2);

                long product = (long) Math.pow(EvaluationCard.PRIMES[pairRank], 2)
                        * (long) EvaluationCard.PRIMES[k1] * (long) EvaluationCard.PRIMES[k2]
                        * (long) EvaluationCard.PRIMES[k3];
                unsuitedLookup.put(product, rank++);
            }
        }

    }

}
