package analyzor.modele.simulation;

import java.util.HashMap;

public class NomsPositions {
    public static HashMap<Integer, String> obtNoms(int nombreJoueurs) {
        // todo faire un algo général quelque soit le nombre de joueurs
        // important les numéros doivent correspondre à l'ordre des actions préflop
        HashMap<Integer, String> nomsPositions = new HashMap<>();
        if (nombreJoueurs == 3) {
            nomsPositions.put(0, "BTN");
            nomsPositions.put(1, "SB");
            nomsPositions.put(2, "BB");
        }
        else throw new IllegalArgumentException("Nombre de joueurs non implémenté");

        return nomsPositions;
    }
}
