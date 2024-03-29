package analyzor.modele.arbre.classificateurs;

import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.Entree;

import java.util.List;


public class ClassificateurDynamique extends Classificateur {
    public ClassificateurDynamique(FormatSolution formatSolution) throws NonImplemente {
        super(formatSolution);
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
