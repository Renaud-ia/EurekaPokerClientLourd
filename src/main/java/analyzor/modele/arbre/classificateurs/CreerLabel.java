package analyzor.modele.arbre.classificateurs;

import analyzor.modele.arbre.noeuds.NoeudDenombrable;
import analyzor.modele.parties.Entree;

import java.util.List;

public interface CreerLabel {
    // labellise la base puis retourne toutes les situations ISO
    List<NoeudDenombrable> obtenirSituations(List<Entree> entreesSituation);
}
