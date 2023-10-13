package analyzor.modele.poker;

import java.util.List;

public class Board {
    private final List<Carte> cartes;
    public Board(List<Carte> cartesBoard) {
        cartes = cartesBoard;
    }

    public int asInt() {
        int boardInt = 0;
        for (Carte carte : cartes) {
            boardInt = (boardInt << Carte.N_BITS_CARTE) | carte.toInt();
        }
        return boardInt;
    }
}
