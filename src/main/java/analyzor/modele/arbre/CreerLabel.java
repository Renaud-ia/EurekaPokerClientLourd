package analyzor.modele.arbre;

import analyzor.modele.parties.Situation;
import analyzor.modele.parties.SituationIso;

import java.util.List;

public interface CreerLabel {
    // labellise la base puis retourne toutes les situations ISO
    List<SituationIso> obtenirSituations(Situation situation);
}
