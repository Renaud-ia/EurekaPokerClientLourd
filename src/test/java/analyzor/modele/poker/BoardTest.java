package analyzor.modele.poker;

import analyzor.modele.utils.Combinations;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardTest {
    @Test
    void egaliteBoard() {
        List<Carte> toutesLesCartes = GenerateurCombos.toutesLesCartes;
        Collections.shuffle(toutesLesCartes);

        List<Carte> echantillonCartes = toutesLesCartes.subList(0, Math.min(toutesLesCartes.size(), 20));

        Combinations<Carte> combinator = new Combinations<>(echantillonCartes);
            for (List<Carte> cartesBoard : combinator.getCombinations(3)) {
                Board boardRandom = new Board(cartesBoard);
                Board boardRecree = new Board(boardRandom.asInt());
                assertEquals(boardRecree, boardRandom, "Problème : le board n'est pas le même");
                System.out.println("*******************");
                System.out.println(boardRandom);
            }
        }
}
