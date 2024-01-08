package analyzor.modele.estimation;

import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Variante;

import java.util.ArrayList;
import java.util.List;

public class GestionnaireFormatTest {
    void recuperationDonnees() {
        Variante.PokerFormat pokerFormat = Variante.PokerFormat.SPIN;
        FormatSolution formatSolution = new FormatSolution(pokerFormat, false, false, 3, 0, 100);
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);

        for (NoeudAbstrait noeudAbstrait : arbreAbstrait.obtenirNoeuds()) {
            List<NoeudAbstrait> listeNoeud = new ArrayList<>();
            listeNoeud.add(noeudAbstrait);
            List<Entree> entreesCorrespondantes = GestionnaireFormat.getEntrees(formatSolution, listeNoeud, null);

        }
    }


}
