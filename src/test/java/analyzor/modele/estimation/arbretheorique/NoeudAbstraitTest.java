package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class NoeudAbstraitTest {

    /**
     * vérifie si un NoeudAbstrait généré à partir de son id long est identique
     */
    @Test
    void reconstructionDuMemeObjetAPartirDuLong() {
        Move[] movesPossibles = new Move[]{Move.FOLD, Move.CALL, Move.RAISE, Move.ALL_IN};
        TourMain.Round[] roundsPossibles =
                new TourMain.Round[]{TourMain.Round.PREFLOP, TourMain.Round.FLOP,
                                    TourMain.Round.TURN, TourMain.Round.RIVER};
        Random random = new Random();

        TourMain.Round randomRound = roundsPossibles[random.nextInt(roundsPossibles.length)];
        int randomJoueurs = random.nextInt(10) + 2;

        int N_TESTS = 100000;

        for (int i = 0; i < N_TESTS; i++) {

            NoeudAbstrait noeudTest = new NoeudAbstrait(randomJoueurs, randomRound);

            int MAX_ITER = 300;
            int compte = 0;
            while (!noeudTest.isLeaf()) {
                assertTrue(compte++ < MAX_ITER);
                int randomIndex = random.nextInt(movesPossibles.length);
                Move randomMove = movesPossibles[randomIndex];
                noeudTest.ajouterAction(randomMove);
            }

            NoeudAbstrait noeudLong = new NoeudAbstrait(noeudTest.toLong());

            assertEquals(noeudLong.toLong(), noeudTest.toLong());
        }
        NoeudAbstrait rootHU = new NoeudAbstrait(2, TourMain.Round.PREFLOP);
        NoeudAbstrait root3WAY = new NoeudAbstrait(3, TourMain.Round.PREFLOP);

        assertNotEquals(rootHU, root3WAY);
    }

}
