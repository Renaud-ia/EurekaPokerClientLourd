package analyzor.modele.parties;

public enum PokerRoom {
    WINAMAX,
    IPOKER,
    POKERSTARS;

    @Override
    public String toString() {
        String result;
        switch (this) {
            case WINAMAX: {
                result = "Winamax";
                break;
            }
            case IPOKER: {
                result = "Betclic";
                break;
            }
            case POKERSTARS: {
                result = "PokerStars";
                break;
            }
            default: {
                result = super.toString();
                break;
            }
        }
        return result;
    }
}
