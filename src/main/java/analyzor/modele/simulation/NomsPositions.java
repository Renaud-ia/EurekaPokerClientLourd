package analyzor.modele.simulation;

import java.util.HashMap;

public class NomsPositions {
    public static HashMap<Integer, String> obtNoms(int nombreJoueurs) {
        // todo faire un algo général quelque soit le nombre de joueurs
        // on veut que BTN soit toujours en position zéro
        // important les numéros doivent correspondre à l'ordre des actions préflop
        HashMap<Integer, String> nomsPositions = new HashMap<>();

        nomsPositions.put(0, "BTN");
        nomsPositions.put(1, "SB");
        nomsPositions.put(2, "BB");

        if (nombreJoueurs <= 6) {
            nomsPositions.put(3, "UTG");
            nomsPositions.put(4, "HJ");
            nomsPositions.put(5, "CO");
        }

        else if (nombreJoueurs <= 7) {
            nomsPositions.put(3, "UTG");
            nomsPositions.put(5, "LJ");
            nomsPositions.put(6, "HJ");
            nomsPositions.put(7, "CO");
        }

        else if (nombreJoueurs <= 8) {
            nomsPositions.put(3, "UTG");
            nomsPositions.put(4, "UTG+1");
            nomsPositions.put(5, "LJ");
            nomsPositions.put(6, "HJ");
            nomsPositions.put(7, "CO");
        }

        else if (nombreJoueurs <= 9) {
            nomsPositions.put(3, "UTG");
            nomsPositions.put(4, "UTG+1");
            nomsPositions.put(5, "UTG+2");
            nomsPositions.put(6, "LJ");
            nomsPositions.put(7, "HJ");
            nomsPositions.put(8, "CO");
        }

        else if (nombreJoueurs <= 10) {
            nomsPositions.put(3, "UTG");
            nomsPositions.put(4, "UTG+1");
            nomsPositions.put(5, "UTG+2");
            nomsPositions.put(6, "MP");
            nomsPositions.put(7, "MP+1");
            nomsPositions.put(8, "HJ");
            nomsPositions.put(9, "CO");
        }

        else {
            throw new IllegalArgumentException("Nombre de joueurs non implémenté");
        }

        return nomsPositions;
    }
}
