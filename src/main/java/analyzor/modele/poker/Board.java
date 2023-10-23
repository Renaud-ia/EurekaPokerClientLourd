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

    public Board(String cartesBoard) {
        cartesBoard = cartesBoard.replace(" ", "");
        this.cartes = new ArrayList<>();

        // Vérifier que la longueur de la chaîne est un nombre pair
        if (cartesBoard.length() % 2 != 0) {
            throw new IllegalArgumentException("Format de board saisi invalide");
        }

        for (int i = 0; i < cartesBoard.length(); i += 2) {
            char currentRank = cartesBoard.charAt(i);
            char currentSuit = cartesBoard.charAt(i + 1);
            cartes.add(new Carte(currentRank, currentSuit));
        }
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

    @Override
    public String toString() {
        StringBuilder repr = new StringBuilder();
        repr.append("Board [");
        for (int i = 0; i < cartes.size(); i++) {
            repr.append(cartes.get(i));
            if (i < cartes.size() - 1) {
                repr.append(", ");
            }
        }
        repr.append("]");
        return repr.toString();
    }
}
