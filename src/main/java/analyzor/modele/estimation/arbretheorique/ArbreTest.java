package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.config.ValeursConfig;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * impossible de tester les leafs dans la base car certaines seront incorrectes!
 */
public class ArbreTest {

    /**
     * vérifie si la génération par long ou par séquence d'action donne le même résultat
     *
     */
    @Test
    void generationId() {
        Move[] movesPossibles = new Move[]{Move.FOLD, Move.CALL, Move.RAISE, Move.ALL_IN};
        TourMain.Round[] roundsPossibles =
                new TourMain.Round[]{TourMain.Round.PREFLOP, TourMain.Round.FLOP,
                                    TourMain.Round.TURN, TourMain.Round.RIVER};
        Random random = new Random();

        TourMain.Round randomRound = roundsPossibles[random.nextInt(roundsPossibles.length)];
        int randomJoueurs = random.nextInt(ValeursConfig.MAX_JOUEURS - 2) + 2;

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

            assertTrue(noeudLong.toLong() == noeudTest.toLong());
        }
        NoeudAbstrait rootHU = new NoeudAbstrait(2, TourMain.Round.PREFLOP);
        NoeudAbstrait root3WAY = new NoeudAbstrait(3, TourMain.Round.PREFLOP);

        assertNotEquals(rootHU, root3WAY);
    }

    /**
     * vérifie que chaque id de l'arbre est unique
     */
    @Test
    void generationArbre() {
        FormatSolution formatSolution = new FormatSolution(Variante.PokerFormat.SPIN, 3);
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        List<NoeudAbstrait> noeudsArbre = arbreAbstrait.obtenirNoeuds();
        List<Long> identifiants = new ArrayList<>();

        NoeudAbstrait noeudCompare = actionComparee();
        float minDistance = 100000;
        NoeudAbstrait noeudPlusProche = null;
        for (NoeudAbstrait noeudAbstrait : arbreAbstrait.obtenirNoeuds()) {
            System.out.println(noeudAbstrait);
            float distance = noeudAbstrait.distanceNoeud(noeudCompare);
            if (distance < minDistance) {
                minDistance = distance;
                noeudPlusProche = noeudAbstrait;
            }
            assertFalse(identifiants.contains(noeudAbstrait.toLong()));
            identifiants.add(noeudAbstrait.toLong());
        }

        System.out.println("NOMBRE D'ACTIONS : " + noeudsArbre.size());
        System.out.println("ACTION COMPAREE : " + noeudCompare);
        System.out.println("ACTION PLUS PROCHE : " + noeudPlusProche);
    }

    NoeudAbstrait actionComparee() {
        NoeudAbstrait noeudCompare = new NoeudAbstrait(3, TourMain.Round.PREFLOP);
        noeudCompare.ajouterAction(Move.CALL);
        noeudCompare.ajouterAction(Move.CALL);
        noeudCompare.ajouterAction(Move.CALL);
        noeudCompare.ajouterAction(Move.CALL);

        return noeudCompare;
    }

}
