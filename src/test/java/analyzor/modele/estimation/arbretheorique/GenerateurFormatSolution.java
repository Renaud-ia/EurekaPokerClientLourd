package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Variante;

/**
 * génère des format solutions pour test
 */
public class GenerateurFormatSolution {
    public static FormatSolution formatMttSixJoueurs() {
        return new FormatSolution(
                null,
                Variante.PokerFormat.MTT,
                0,
                20,
                0,
                20,
                false,
                6,
                0,
                100,
                null,
                null
        );
    }

    public static FormatSolution formatMttNeufJoueurs() {
        return new FormatSolution(
                null,
                Variante.PokerFormat.MTT,
                0,
                20,
                0,
                20,
                false,
                9,
                0,
                100,
                null,
                null
        );
    }

    public static FormatSolution formatCashGameCinqJoueurs() {
        return new FormatSolution(
                null,
                Variante.PokerFormat.CASH_GAME,
                0,
                20,
                0,
                20,
                false,
                5,
                0,
                100,
                null,
                null
        );
    }

    public static FormatSolution formatSpinTroisJoueurs() {
        return new FormatSolution(
                null,
                Variante.PokerFormat.SPIN,
                0,
                20,
                0,
                20,
                false,
                3,
                0,
                100,
                null,
                null
        );
    }
}
