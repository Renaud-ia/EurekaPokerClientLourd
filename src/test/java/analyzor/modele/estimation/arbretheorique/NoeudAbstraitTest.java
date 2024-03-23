package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class NoeudAbstraitTest {
    @Test
    void nombreDeJoueursDifferentsDonneDesNoeudsInitiauxDifferents() {
        NoeudAbstrait rootHU = new NoeudAbstrait(2, TourMain.Round.PREFLOP);
        NoeudAbstrait root3WAY = new NoeudAbstrait(3, TourMain.Round.PREFLOP);

        assertNotEquals(rootHU.toLong(), root3WAY.toLong());
    }

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
    }

    @Test
    void testCopie() {
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

            NoeudAbstrait noeudCopie = noeudTest.copie();

            assertNotSame(noeudCopie, noeudTest);
            assertEquals(noeudCopie.toLong(), noeudTest.toLong());
        }
    }

    @Test
    void limitationDesActionsFonctionne() {
        /*
         * TEST 1
         *
         */
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(6, TourMain.Round.PREFLOP);
        noeudAbstrait.ajouterAction(Move.CALL);
        noeudAbstrait.ajouterAction(Move.CALL);
        noeudAbstrait.ajouterAction(Move.CALL);

        assertTrue(noeudAbstrait.maxActionsAtteint(3));

        /*
         * TEST 2
         *
         */
        noeudAbstrait = new NoeudAbstrait(6, TourMain.Round.PREFLOP);
        noeudAbstrait.ajouterAction(Move.FOLD);
        noeudAbstrait.ajouterAction(Move.FOLD);
        noeudAbstrait.ajouterAction(Move.CALL);
        noeudAbstrait.ajouterAction(Move.CALL);
        noeudAbstrait.ajouterAction(Move.RAISE);

        assertTrue(noeudAbstrait.maxActionsAtteint(3));

        /*
         * TEST 3
         *
         */
        noeudAbstrait = new NoeudAbstrait(3, TourMain.Round.PREFLOP);
        noeudAbstrait.ajouterAction(Move.RAISE);
        noeudAbstrait.ajouterAction(Move.ALL_IN);
        noeudAbstrait.ajouterAction(Move.CALL);

        assertFalse(noeudAbstrait.maxActionsAtteint(3));

        /*
         * TEST 4
         *
         */
        noeudAbstrait = new NoeudAbstrait(4, TourMain.Round.PREFLOP);
        noeudAbstrait.ajouterAction(Move.FOLD);
        noeudAbstrait.ajouterAction(Move.CALL);
        noeudAbstrait.ajouterAction(Move.RAISE);
        noeudAbstrait.ajouterAction(Move.RAISE);
        noeudAbstrait.ajouterAction(Move.CALL);

        assertFalse(noeudAbstrait.maxActionsAtteint(3));

        /*
         * TEST 5
         *
         */
        noeudAbstrait = new NoeudAbstrait(4, TourMain.Round.PREFLOP);
        noeudAbstrait.ajouterAction(Move.RAISE);
        noeudAbstrait.ajouterAction(Move.RAISE);
        noeudAbstrait.ajouterAction(Move.RAISE);

        assertTrue(noeudAbstrait.maxActionsAtteint(3));

        /*
         * TEST 6
         *
         */
        noeudAbstrait = new NoeudAbstrait(4, TourMain.Round.PREFLOP);
        noeudAbstrait.ajouterAction(Move.FOLD);
        noeudAbstrait.ajouterAction(Move.RAISE);
        noeudAbstrait.ajouterAction(Move.RAISE);
        noeudAbstrait.ajouterAction(Move.RAISE);

        assertFalse(noeudAbstrait.maxActionsAtteint(3));

        /*
         * TEST 7
         *
         */
        noeudAbstrait = new NoeudAbstrait(3, TourMain.Round.PREFLOP);
        noeudAbstrait.ajouterAction(Move.RAISE);
        noeudAbstrait.ajouterAction(Move.RAISE);
        noeudAbstrait.ajouterAction(Move.RAISE);
        noeudAbstrait.ajouterAction(Move.RAISE);

        assertFalse(noeudAbstrait.maxActionsAtteint(3));
    }

}
