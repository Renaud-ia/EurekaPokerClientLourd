package analyzor.modele.arbre.classificateurs;

import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.estimation.CalculInterrompu;
import analyzor.modele.parties.Entree;

import java.util.List;

public interface CreerLabel {

    void creerSituations(List<Entree> entreesSituation) throws CalculInterrompu;

    boolean construireCombosDenombrables();
    List<NoeudDenombrable> obtenirSituations();
}
