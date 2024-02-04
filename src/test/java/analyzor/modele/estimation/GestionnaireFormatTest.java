package analyzor.modele.estimation;

import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Variante;

import java.util.ArrayList;
import java.util.List;

public class GestionnaireFormatTest {
    void recuperationEntreesBonFormat() {
        FormatSolution formatSolution = new FormatSolution(
                "Mon format",
                Variante.PokerFormat.SPIN,
                0,
                20,
                0,
                20,
                false,
                3,
                0,
                100
        );

    }


}
