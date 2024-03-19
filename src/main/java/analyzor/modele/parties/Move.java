package analyzor.modele.parties;

public enum Move {
    //todo : déterminer le montant des actions en deux temps (dans Simulation?)
    // important, ceci va déterminer l'ordre d'affichage des actions
    FOLD, CALL, RAISE, ALL_IN, CHECK_RAISE, RAISE_CALL, RAISE_RAISE;

    public static int nombreMovesUneStreet() {
        return 4;
    }

    public int distance(Move autreMove) {
        return Math.abs(this.ordinal() - autreMove.ordinal());
    }

    @Override
    public String toString() {
        String result;
        switch (this) {
            case FOLD: {
                result = "Fold";
                break;
            }
            case CALL: {
                result = "Call";
                break;
            }
            case RAISE: {
                result = "Raise";
                break;
            }
            case ALL_IN: {
                result = "All-in";
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
