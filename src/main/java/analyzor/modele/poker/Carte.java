package analyzor.modele.poker;

import analyzor.modele.utils.Bits;

import java.util.HashMap;
import java.util.Map;

import static analyzor.modele.utils.Bits.creerMasque;

public class Carte {
    

    
    public static final Character[] STR_RANKS;
    public static final Character[] STR_SUITS;
    public static final Map<Character, Integer> CHAR_RANK_TO_INT_RANK = new HashMap<>();
    public static final Map<Integer, Character> INT_RANK_TO_CHAR_RANK = new HashMap<>();
    public static final Map<Character, Integer> CHAR_SUIT_TO_INT_SUIT = new HashMap<>();
    public static final Map<Integer, Character> INT_SUIT_TO_CHAR_SUIT = new HashMap<>();
    public static int CARTE_MAX;
    protected static final int N_BITS_RANK;
    private static final int N_BITS_SUIT;
    public static final int N_BITS_CARTE;
    private static final int MASK_SUIT;

    static {
        
        STR_RANKS = new Character[]{'2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A'};
        STR_SUITS = new Character[]{'s', 'h', 'd', 'c'};
        for (int i = 0; i < STR_RANKS.length; i++) {
            CHAR_RANK_TO_INT_RANK.put(STR_RANKS[i], i);
            INT_RANK_TO_CHAR_RANK.put(i, STR_RANKS[i]);
        }
        for (int i = 0; i < STR_SUITS.length; i++) {
            CHAR_SUIT_TO_INT_SUIT.put(STR_SUITS[i], i);
            INT_SUIT_TO_CHAR_SUIT.put(i, STR_SUITS[i]);
        }
        
        N_BITS_RANK = Bits.bitsNecessaires(CHAR_RANK_TO_INT_RANK.size() + 1);
        N_BITS_SUIT = Bits.bitsNecessaires(CHAR_SUIT_TO_INT_SUIT.size());
        N_BITS_CARTE = N_BITS_RANK + N_BITS_SUIT;
        MASK_SUIT = (int) creerMasque(N_BITS_RANK, N_BITS_SUIT);
        
        CARTE_MAX = new Carte(STR_RANKS[STR_RANKS.length - 1], STR_SUITS[STR_SUITS.length - 1]).toInt();
    }

    
    private final int intCode;
    
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

        
        this.intCode = ((intRank + 1) << N_BITS_SUIT) | intSuit;
    }

    public Carte(int intCard) {
        this.intCode = intCard;
        
        this.intRank = (intCode >> N_BITS_SUIT) - 1;
        this.intSuit = intCode & MASK_SUIT;
    }

    @Override
    public String toString() {
        if (rank == null && suit == null) {
            rank = INT_RANK_TO_CHAR_RANK.get(intRank);
            suit = INT_SUIT_TO_CHAR_SUIT.get(intSuit);
        }
        
        return "Carte (" + rank + suit + ") : " + intCode;
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

    public Carte copie() {
        return new Carte(intCode);
    }

}
