package analyzor.modele.parties;

public enum PokerRoom {
    WINAMAX,
    IPOKER,
    POKERSTARS;

    @Override
    public String toString() {
        return switch (this) {
            case WINAMAX -> "Winamax";
            case IPOKER -> "iPoker Network (Betclic, Unibet)";
            case POKERSTARS -> "PokerStars";
            default -> super.toString();
        };
    }
}
