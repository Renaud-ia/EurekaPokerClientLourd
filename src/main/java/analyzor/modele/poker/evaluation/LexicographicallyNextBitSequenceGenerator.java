package analyzor.modele.poker.evaluation;

public class LexicographicallyNextBitSequenceGenerator {
    private int current;

    public LexicographicallyNextBitSequenceGenerator(int bits) {
        this.current = getNextLexicographicallyNextBitSequence(bits);
    }

    public int next() {
        int result = current;
        current = getNextLexicographicallyNextBitSequence(current);
        return result;
    }

    private int getNextLexicographicallyNextBitSequence(int bits) {
        int t = (bits | (bits - 1)) + 1;
        int next = t | (((t & -t) / (bits & -bits) >> 1) - 1);
        return next;
    }

}