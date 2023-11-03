package analyzor.modele.arbre;

import analyzor.modele.parties.Entree;

import java.util.List;

public interface CreerLabel {
    // labellise la base puis retourne toutes les situations ISO
    List<NoeudAvecRange> obtenirSituations(List<Entree> entreesSituation);
}
