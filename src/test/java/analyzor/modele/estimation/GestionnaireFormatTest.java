package analyzor.modele.estimation;

import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import analyzor.modele.utils.RequetesBDD;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;

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
