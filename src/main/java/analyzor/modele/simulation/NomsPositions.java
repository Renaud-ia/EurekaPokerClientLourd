package analyzor.modele.simulation;

import java.util.HashMap;

public class NomsPositions {
    public static HashMap<Integer, String> obtNoms(int nombreJoueurs) {
        // todo faire un algo général quelque soit le nombre de joueurs
        // on veut que BTN soit toujours en position zéro
        // important les numéros doivent correspondre à l'ordre des actions préflop
        HashMap<Integer, String> nomsPositions = new HashMap<>();

        if (nombreJoueurs == 3) {
            nomsPositions.put(0, "BTN");
            nomsPositions.put(1, "SB");
            nomsPositions.put(2, "BB");
        }

        else if (nombreJoueurs == 6) {
            nomsPositions.put(0, "BTN");
            nomsPositions.put(1, "SB");
            nomsPositions.put(2, "BB");
            nomsPositions.put(3, "UTG");
            nomsPositions.put(4, "HJ");
            nomsPositions.put(5, "CO");
        }

        else if (nombreJoueurs == 5) {
            nomsPositions.put(0, "BTN");
            nomsPositions.put(1, "SB");
            nomsPositions.put(2, "BB");
            nomsPositions.put(3, "MP");
            nomsPositions.put(4, "CO");
        }

        else {
            throw new IllegalArgumentException("Nombre de joueurs non implémenté");
        }

        return nomsPositions;
    }
}
