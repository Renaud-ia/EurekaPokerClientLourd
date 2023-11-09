package analyzor.modele.parties;

public enum Move {
    //todo : déterminer le montant des actions en deux temps (dans Simulation?)
    FOLD, CALL, RAISE, ALL_IN, CHECK_RAISE, RAISE_CALL, RAISE_RAISE;

    public static int nombreMovesUneStreet() {
        return 4;
    }

    public int distance(Move autreMove) {
        return Math.abs(this.ordinal() - autreMove.ordinal());
    }
}
