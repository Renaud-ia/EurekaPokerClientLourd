package analyzor.modele.arbre.classificateurs;

import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.parties.Entree;

import java.util.List;

public interface CreerLabel {
    // labellise la base puis retourne toutes les situations ISO
    void creerSituations(List<Entree> entreesSituation);
    /**
     * découplé de la construction des noeuds pour plus de flexibilité
     * chaque classificateur appelle la bonne méthode
     */
    void construireCombosDenombrables();
    List<NoeudDenombrable> obtenirSituations();
}
