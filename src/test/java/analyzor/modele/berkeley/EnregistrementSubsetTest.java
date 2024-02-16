package analyzor.modele.berkeley;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import analyzor.modele.poker.Deck;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnregistrementSubsetTest {
    @Test
    void testEnregistrement() {
        // todo remove le subset après??
        final int N_BOARDS = 100;
        final int TAILLE_BOARD = 3;
        Deck deck = new Deck();
        List<Board> boardsOrigine = new ArrayList<>();
        for (int i = 0; i < N_BOARDS; i++) {
            deck.remplir();
            List<Carte> cartesBoard = new ArrayList<>();
            for (int j = 0; j < TAILLE_BOARD; j++) {
                cartesBoard.add(deck.carteRandom());
            }
            Board boardRandom = new Board(cartesBoard);
            boardsOrigine.add(boardRandom);
        }

        EnregistrementSubset enregistrementSubset = new EnregistrementSubset();
        try {
            int nombreSubsets = boardsOrigine.size();
            enregistrementSubset.enregistrerSubsets(boardsOrigine);
            List<Board> boardsRecuperes = enregistrementSubset.recupererSubsets(nombreSubsets);

            for (Board boardOriginal : boardsOrigine) {
                System.out.println("BOARD CHERCHE : " + boardOriginal);
                assertTrue(boardsRecuperes.contains(boardOriginal));
            }

        }
        catch (Exception e) {
            System.out.println("ERREUR DATABASE");
        }
    }
}
