package analyzor.modele.poker;

import analyzor.modele.utils.Combinations;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    @Test
    void idenfifiantBoardEstUnique() {
        Deck deck = new Deck();

        final int N_TESTS = 100000;
        for (int i = 0; i < N_TESTS; i++) {
            deck.remplir();

            Random random = new Random();

            int randomTaille = random.nextInt(3, 5);
            List<Carte> cartesPremierBoard = new ArrayList<>();
            for (int nCarte = 0; nCarte <= randomTaille; nCarte++) {
                cartesPremierBoard.add(deck.carteRandom());
            }

            Board boardRandom1 = new Board(cartesPremierBoard);

            randomTaille = random.nextInt(3, 5);
            List<Carte> cartesSecondBoard = new ArrayList<>();
            for (int nCarte = 0; nCarte <= randomTaille; nCarte++) {
                cartesSecondBoard.add(deck.carteRandom());
            }

            Board boardRandom2 = new Board(cartesSecondBoard);

            assertNotEquals(boardRandom1.asInt(), boardRandom2.asInt());
        }
    }

    @Test
    void boardReconstruitAvecIdEstLeMeme() {
        List<Carte> toutesLesCartes = GenerateurCombos.toutesLesCartes;
        Collections.shuffle(toutesLesCartes);

        List<Carte> echantillonCartes = toutesLesCartes.subList(0, Math.min(toutesLesCartes.size(), 20));

        Combinations<Carte> combinator = new Combinations<>(echantillonCartes);
            for (List<Carte> cartesBoard : combinator.getCombinations(3)) {
                Board boardRandom = new Board(cartesBoard);
                Board boardRecree = new Board(boardRandom.asInt());
                assertEquals(boardRecree, boardRandom, "Problème : le board n'est pas le même");
            }
    }
}
