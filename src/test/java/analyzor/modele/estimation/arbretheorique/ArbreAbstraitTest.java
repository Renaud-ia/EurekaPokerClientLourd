package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.estimation.FormatSolution;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ArbreAbstraitTest {
    @Test
    void aucunNoeudIdentiqueMtt() {
        FormatSolution formatSolution = GenerateurFormatSolution.formatMttSixJoueurs();
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);

        List<NoeudAbstrait> noeudAbstrait = arbreAbstrait.obtenirNoeuds();

        for (int i = 0; i < noeudAbstrait.size(); i++) {
            for (int j = 0; j < noeudAbstrait.size(); j++) {
                if (i == j) continue;
                assertNotEquals(noeudAbstrait.get(i).toLong(), noeudAbstrait.get(j).toLong());
            }
        }

        FormatSolution formatSolutionNeufJoueurs = GenerateurFormatSolution.formatMttSixJoueurs();
        ArbreAbstrait arbreAbstraitNeufsJoueurs = new ArbreAbstrait(formatSolutionNeufJoueurs);

        List<NoeudAbstrait> noeudsGeneres = arbreAbstraitNeufsJoueurs.obtenirNoeuds();

        for (int i = 0; i < noeudsGeneres.size(); i++) {
            for (int j = 0; j < noeudsGeneres.size(); j++) {
                if (i == j) continue;
                assertNotEquals(noeudsGeneres.get(i).toLong(), noeudsGeneres.get(j).toLong());
            }
        }
    }

    @Test
    void aucunNoeudIdentiqueSpin() {
        FormatSolution formatSolution = GenerateurFormatSolution.formatSpinTroisJoueurs();
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);

        List<NoeudAbstrait> noeudAbstrait = arbreAbstrait.obtenirNoeuds();

        for (int i = 0; i < noeudAbstrait.size(); i++) {
            for (int j = 0; j < noeudAbstrait.size(); j++) {
                if (i == j) continue;
                assertNotEquals(noeudAbstrait.get(i).toLong(), noeudAbstrait.get(j).toLong());
            }
        }
    }

    @Test
    void aucunNoeudIdentiqueCashGame() {
        FormatSolution formatSolution = GenerateurFormatSolution.formatCashGameCinqJoueurs();
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);

        List<NoeudAbstrait> noeudAbstrait = arbreAbstrait.obtenirNoeuds();

        for (int i = 0; i < noeudAbstrait.size(); i++) {
            for (int j = 0; j < noeudAbstrait.size(); j++) {
                if (i == j) continue;
                assertNotEquals(noeudAbstrait.get(i).toLong(), noeudAbstrait.get(j).toLong());
            }
        }
    }

    @Test
    void noeudPrecedentDuSuivantRevientMemeNoeud() {
        FormatSolution formatSolution = GenerateurFormatSolution.formatCashGameCinqJoueurs();
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);

        List<NoeudAbstrait> noeudArbre = arbreAbstrait.obtenirNoeuds();

        for (NoeudAbstrait noeudAbstraitInitial : noeudArbre) {
            for (NoeudAbstrait noeudSuivant : arbreAbstrait.noeudsSuivants(noeudAbstraitInitial)) {
                NoeudAbstrait noeudPrecedent = arbreAbstrait.noeudPrecedent(noeudSuivant);

                assertEquals(noeudAbstraitInitial, noeudPrecedent);
            }
        }
    }

    @Test
    void noeudSuivantUneActionDePlus() {
        FormatSolution formatSolution = GenerateurFormatSolution.formatCashGameCinqJoueurs();
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);

        List<NoeudAbstrait> noeudArbre = arbreAbstrait.obtenirNoeuds();

        for (NoeudAbstrait noeudAbstraitInitial : noeudArbre) {
            for (NoeudAbstrait noeudSuivant : arbreAbstrait.noeudsSuivants(noeudAbstraitInitial)) {
                int nActionsInitial = noeudAbstraitInitial.nombreActions();
                int nActionsSuivantes = noeudSuivant.nombreActions();

                assertEquals(nActionsSuivantes, nActionsInitial + 1);
            }
        }
    }
}
