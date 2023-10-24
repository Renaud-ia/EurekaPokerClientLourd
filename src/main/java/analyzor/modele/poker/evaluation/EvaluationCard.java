package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.Carte;

import java.util.List;

public class EvaluationCard {
    /**
     *                                 EvaluationCard:
     *
     *                           bitrank     suit rank   prime
     *                     +--------+--------+--------+--------+
     *                     |xxxbbbbb|bbbbbbbb|cdhsrrrr|xxpppppp|
     *                     +--------+--------+--------+--------+
     *
     *         1) p = nombre premier correspondant au rank
     *         2) r = rank de la carte
     *         3) cdhs = suit de la carte
     *         4) b = bit dépendant du rank
     *         5) x = unused
     */
    // the basics
    public static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};
    protected static final int[] INT_SUIT_TO_BINARY_SUIT = {1, 2, 4, 8};

    public static int newCard(Carte carte) {
        int rankInt = carte.getIntRank();
        int suitInt = INT_SUIT_TO_BINARY_SUIT[carte.getIntSuit()];
        int rankPrime = PRIMES[rankInt];

        int bitrank = 1 << rankInt << 16;
        int suit = suitInt << 12;
        int rank = rankInt << 8;

        return bitrank | suit | rank | rankPrime;
    }

    protected static long primeProductFromHand(List<Integer> cardsInts) {
        long product = 1;
        for (int c : cardsInts) {
            product *= c & 0xFF;
        }
        return product;
    }

    protected static long primeProductFromRankbits(int rankbits) {
        long product = 1;
        for (int i : Carte.CHAR_RANK_TO_INT_RANK.values()) {
            if ((rankbits & (1 << i)) != 0) {
                product *= PRIMES[i];
            }
        }
        return product;
    }
}
