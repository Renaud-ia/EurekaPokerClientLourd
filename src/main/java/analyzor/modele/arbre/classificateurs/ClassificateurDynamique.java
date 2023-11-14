package analyzor.modele.arbre.classificateurs;

import analyzor.modele.equilibrage.NoeudDenombrable;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.exceptions.*;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

/**
 * classificateur qui clusterise dynamiquement les situations selon la distribution des ranges
 * attention : on a besoin d'avoir calculé les ranges des Situation précédentes
 */
public class ClassificateurDynamique extends Classificateur {
    public ClassificateurDynamique() throws NonImplemente {
        throw new NonImplemente();
    }

    @Override
    public List<NoeudDenombrable> obtenirSituations(List<Entree> entreesSituation, FormatSolution formatSolution) {
        //todo
        return new ArrayList<>();
    }
}
