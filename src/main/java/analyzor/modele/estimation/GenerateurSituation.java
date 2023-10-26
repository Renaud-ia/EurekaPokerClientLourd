package analyzor.modele.estimation;

import analyzor.modele.parties.Situation;
import analyzor.modele.parties.TourMain;

import java.util.ArrayList;
import java.util.List;

/**
 * crée les Situation (=arbre) selon des critères modifiables
 */
public class GenerateurSituation {
    private final int maxActionParJoueur;
    private final int nombreJoueurs;
    public GenerateurSituation(int nombreJoueurs) {
        //todo variable par round, type tournoi?
        maxActionParJoueur = 2;
        this.nombreJoueurs = nombreJoueurs;
    }

    public static List<Situation> getSituations(TourMain.Round round) {
        //todo
        // regarder si SUBSETS 2E RANK => si oui, on ne génère pas le 2e rang d'action au flop
        return new ArrayList<>();
    }

    public static boolean estLeaf(Situation situation) {
        return false;
    }
}
