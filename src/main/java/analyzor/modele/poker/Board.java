package analyzor.modele.poker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Board {
    private final List<Carte> cartes;

    //utile pour calculer des valeurs préflop
    public Board() {this.cartes = new ArrayList<>();}
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

    public List<Carte> getCartes() {
        // retourne une référence directe car besoin de performances
        return cartes;
    }

    public void ajouterCarte(Carte carte) {
        this.cartes.add(carte);
    }

    public int taille() {
        return cartes.size();
    }

    public Board copie() {
        List<Carte> copieBoard = new ArrayList<>();
        for (Carte carte : this.cartes) {
            Carte carteCopiee = carte.copie();
            copieBoard.add(carteCopiee);
        }

        return new Board(copieBoard);
    }
}
