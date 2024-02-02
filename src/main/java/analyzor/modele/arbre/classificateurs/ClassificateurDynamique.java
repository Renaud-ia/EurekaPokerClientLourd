package analyzor.modele.arbre.classificateurs;

import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.exceptions.TacheInterrompue;
import analyzor.modele.parties.Entree;

import java.util.List;

/**
 * classificateur qui clusterise dynamiquement les situations selon la distribution des ranges
 * attention : on a besoin d'avoir calculé les ranges des Situation précédentes
 */
public class ClassificateurDynamique extends Classificateur {
    public ClassificateurDynamique(FormatSolution formatSolution) throws NonImplemente, TacheInterrompue {
        super();
        throw new NonImplemente();
    }

    @Override
    public void creerSituations(List<Entree> entreesSituation) {

    }

    @Override
    public boolean construireCombosDenombrables() {
        return false;
    }

    @Override
    public List<NoeudDenombrable> obtenirSituations() {
        return null;
    }
}
