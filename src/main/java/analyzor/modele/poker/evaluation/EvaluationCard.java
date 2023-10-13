package analyzor.modele.poker.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EvaluationCard {
    // the basics
    private static final String STR_RANKS = "23456789TJQKA";
    protected static final int[] INT_RANKS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    protected static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};
    private static final Map<Character, Integer> CHAR_RANK_TO_INT_RANK = new HashMap<>();
    private static final Map<Character, Integer> CHAR_SUIT_TO_INT_SUIT = new HashMap<>();
    private static final char[] INT_SUIT_TO_CHAR_SUIT = {'x', 's', 'h', 'x', 'd', 'x', 'x', 'x', 'c'};
    //todo : on s'en fout ? (=débug)
    //private static final Map<Integer, Character> PRETTY_SUITS = new HashMap<>();
    //private static final int[] PRETTY_REDS = {2, 4};

    static {
        for (int i = 0; i < STR_RANKS.length(); i++) {
            CHAR_RANK_TO_INT_RANK.put(STR_RANKS.charAt(i), INT_RANKS[i]);
        }
        CHAR_SUIT_TO_INT_SUIT.put('s', 1);
        CHAR_SUIT_TO_INT_SUIT.put('h', 2);
        CHAR_SUIT_TO_INT_SUIT.put('d', 4);
        CHAR_SUIT_TO_INT_SUIT.put('c', 8);
        //todo : on s'en fout ? (=débug)
        /*
        PRETTY_SUITS.put(1, '\u2660');
        PRETTY_SUITS.put(2, '\u2665');
        PRETTY_SUITS.put(4, '\u2666');
        PRETTY_SUITS.put(8, '\u2663');
         */
    }

    public static int newCard(String string) {
        char rankChar = string.charAt(0);
        char suitChar = string.charAt(1);
        int rankInt = CHAR_RANK_TO_INT_RANK.get(rankChar);
        int suitInt = CHAR_SUIT_TO_INT_SUIT.get(suitChar);
        int rankPrime = PRIMES[rankInt];

        int bitrank = 1 << rankInt << 16;
        int suit = suitInt << 12;
        int rank = rankInt << 8;

        return bitrank | suit | rank | rankPrime;
    }

    public static String intToStr(int cardInt) {
        int rankInt = EvaluationCard.getRankInt(cardInt);
        int suitInt = EvaluationCard.getSuitInt(cardInt);
        return STR_RANKS.charAt(rankInt) + String.valueOf(INT_SUIT_TO_CHAR_SUIT[suitInt]);
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

    public static int[] handToBinary(String[] cardStrs) {
        int[] bhand = new int[cardStrs.length];
        for (int i = 0; i < cardStrs.length; i++) {
            bhand[i] = newCard(cardStrs[i]);
        }
        return bhand;
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
        for (int i : INT_RANKS) {
            //todo est ce que c'est bon ????
            if ((rankbits & (1 << i)) == 1) {
                product *= PRIMES[i];
            }
        }
        return product;
    }

    protected static String intToBinary(int cardInt) {
        //todo : uniquement pour debug
        return "intToBinary non implémenté";
    }

    protected static String intToPrettyStr(int cardInt) {
        //todo : uniquement pour débug
        return "intToPrettyStr non implémenté";
    }

    protected static void printPrettyCard(int cardInt) {
        //todo c'est quoi ce truc dégueu
        System.out.print(intToPrettyStr(cardInt));
    }

    protected static void printPrettyCards(int[] cardInts) {
        StringBuilder output = new StringBuilder(" ");
        for (int i=0; i < cardInts.length; i++) {
            int c = cardInts[i];
            if (i != cardInts.length - 1) {
                output.append(intToPrettyStr(c)).append(", ");
            }
            else output.append(intToPrettyStr(c));
        }
        System.out.print(output);
    }

}
