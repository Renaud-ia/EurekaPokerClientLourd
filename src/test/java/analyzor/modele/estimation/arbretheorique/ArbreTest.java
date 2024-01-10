package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.config.ValeursConfig;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.GestionnaireFormat;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * tests sur Arbre Abstrait
 * impossible de tester les leafs dans la base car certaines seront incorrectes!
 * ex => 2way raise/all-in peut être leaf si le 2e stack est inférieur au premier
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
        FormatSolution formatSolution =
                new FormatSolution(Variante.PokerFormat.MTT, false, false, 5, 0, 100);
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
        NoeudAbstrait noeudCompare = new NoeudAbstrait(5, TourMain.Round.PREFLOP);
        noeudCompare.ajouterAction(Move.CALL);
        noeudCompare.ajouterAction(Move.CALL);
        noeudCompare.ajouterAction(Move.RAISE);
        noeudCompare.ajouterAction(Move.RAISE);
        noeudCompare.ajouterAction(Move.RAISE);

        return noeudCompare;
    }

    // vérifie que tous les noeuds dans la BDD ont un noeud précédent
    @Test
    void labellisationNoeuds() {
        FormatSolution formatSolution =
                new FormatSolution(Variante.PokerFormat.SPIN, false, false, 3, 0, 100);
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        List<NoeudAbstrait> noeudsArbres = arbreAbstrait.obtenirNoeuds();

        List<Entree> toutesLesSituations = GestionnaireFormat.getEntrees(formatSolution, noeudsArbres, null);
        if (toutesLesSituations.isEmpty()) return;

        LinkedHashMap<NoeudAbstrait, List<Entree>> situationsGroupees = arbreAbstrait.trierEntrees(toutesLesSituations);

        Set<NoeudAbstrait> keys = situationsGroupees.keySet();
        List<NoeudAbstrait> listeCles = new ArrayList<>(keys);
        List<Entree> echantillon = situationsGroupees.get(listeCles.get(0));

        int nombreNoeudsInvalides = 0;
        for (Entree entree : echantillon) {
            NoeudAbstrait noeudAbstrait = new NoeudAbstrait(entree.getIdNoeudTheorique());
            if (noeudAbstrait.getRound() == TourMain.Round.RIVER
                    || noeudAbstrait.getRound() == TourMain.Round.TURN) continue;

            System.out.println("Noeud : " + noeudAbstrait);
            if (!noeudAbstrait.isValide()) {
                nombreNoeudsInvalides++;
                continue;
            }

            NoeudAbstrait noeudPlusProche = arbreAbstrait.noeudsPlusProches(noeudAbstrait).get(0);
            if (noeudAbstrait != noeudPlusProche) System.out.println("Noeud plus proche : " + noeudPlusProche);

            NoeudAbstrait noeudPrecedent = arbreAbstrait.noeudPrecedent(noeudPlusProche);
            System.out.println("Noeud précédent : " + noeudPrecedent);
            assertNotNull(noeudPrecedent);

        }
        float pctNoeudInvalides = (float) nombreNoeudsInvalides / echantillon.size();
        System.out.println("Noeuds invalides : " + nombreNoeudsInvalides +  " / " +  echantillon.size());
        assertTrue(pctNoeudInvalides < 0.01);
    }

}
