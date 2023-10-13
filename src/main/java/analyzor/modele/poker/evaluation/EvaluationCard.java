package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.Carte;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EvaluationCard {
    // the basics
    protected static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};
    private static final char[] INT_SUIT_TO_CHAR_SUIT = {'x', 's', 'h', 'x', 'd', 'x', 'x', 'x', 'c'};

    public static int newCard(Carte carte) {
        int rankInt = carte.getIntRank();
        int suitInt = carte.getIntSuit();
        int rankPrime = PRIMES[rankInt];

        int bitrank = 1 << rankInt << 16;
        int suit = suitInt << 12;
        int rank = rankInt << 8;

        return bitrank | suit | rank | rankPrime;
    }

    public static int getRankInt(int cardInt) {
        return (cardInt >> 8) & 0xF;
    }

    public static int getSuitInt(int cardInt) {
        return (cardInt >> 12) & 0xF;
    }

    public static int getBitrankInt(int cardInt) {
        return (cardInt >> 16) & 0x1FFF;
    }

    public static int getPrime(int cardInt) {
        return cardInt & 0x3F;
    }


    protected static int primeProductFromHand(List<Integer> cardsInts) {
        int product = 1;
        for (int c : cardsInts) {
            product *= c & 0xFF;
        }
        return product;
    }

    protected static int primeProductFromRankbits(int rankbits) {
        int product = 1;
        for (int i : Carte.CHAR_RANK_TO_INT_RANK.values()) {
            if ((rankbits & (1 << i)) == 1) {
                product *= PRIMES[i];
            }
        }
        return product;
    }
}
