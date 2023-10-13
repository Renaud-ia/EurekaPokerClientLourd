package analyzor.modele.poker;

import analyzor.modele.utils.Bits;

import java.util.HashMap;
import java.util.Map;

import static analyzor.modele.utils.Bits.creerMasque;

public class Carte {
    // référencement des ranks et suits
    public static final Character[] STR_RANKS =
            {'2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A'};
    public static final Character[] STR_SUITS = {'s', 'h', 'd', 'c'};
    public static final Map<Character, Integer> CHAR_RANK_TO_INT_RANK = new HashMap<>();
    public static final Map<Integer, Character> INT_RANK_TO_CHAR_RANK = new HashMap<>();
    public static final Map<Character, Integer> CHAR_SUIT_TO_INT_SUIT = new HashMap<>();
    public static final Map<Integer, Character> INT_SUIT_TO_CHAR_SUIT = new HashMap<>();
    static {
        for (int i = 0; i < STR_RANKS.length; i++) {
            CHAR_RANK_TO_INT_RANK.put(STR_RANKS[i], i);
            INT_RANK_TO_CHAR_RANK.put(i, STR_RANKS[i]);
        }
        for (int i = 0; i < STR_SUITS.length; i++) {
            CHAR_SUIT_TO_INT_SUIT.put(STR_SUITS[i], i);
            INT_SUIT_TO_CHAR_SUIT.put(i, STR_SUITS[i]);
        }
    }

    private static final int N_BITS_RANK = Bits.bitsNecessaires(CHAR_RANK_TO_INT_RANK.size());
    private static final int N_BITS_SUIT = Bits.bitsNecessaires(CHAR_SUIT_TO_INT_SUIT.size());
    public static final int N_BITS_CARTE = N_BITS_RANK + N_BITS_SUIT;
    private static final int MASK_SUIT = creerMasque(N_BITS_RANK, N_BITS_SUIT);

    // informations de la carte
    private final int intCode;
    // on garde comme String car on veut null si non initialisé
    private Character rank;
    private Character suit;
    private final int intRank;
    private final int intSuit;

    public Carte(char rank, char suit) {
        try {
            intRank = CHAR_RANK_TO_INT_RANK.get(Character.toUpperCase(rank));
            intSuit = CHAR_SUIT_TO_INT_SUIT.get(Character.toLowerCase(suit));
        }
        catch (NullPointerException e){
            throw new NullPointerException("Le rank/suit saisi n'est pas bon");
        }
        this.rank = rank;
        this.suit = suit;

        this.intCode = (intRank << N_BITS_SUIT) | intSuit;
    }

    public Carte(int intCard) {
        this.intCode = intCard;
        this.intRank = intCode >> N_BITS_SUIT;
        this.intSuit = intCode & MASK_SUIT;
    }

    @Override
    public String toString() {
        if (rank == null && suit == null) {
            rank = INT_RANK_TO_CHAR_RANK.get(intRank);
            suit = INT_SUIT_TO_CHAR_SUIT.get(intSuit);
        }
        //todo retourner une forme jolie ?
        return "Carte (" + suit + rank + ") ";
    }

    public int toInt() {
        return intCode;
    }

    public int getIntRank() {
        return intRank;
    }

    public int getIntSuit() {
        return intSuit;
    }
}
