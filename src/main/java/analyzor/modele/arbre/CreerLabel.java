package analyzor.modele.arbre;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Situation;
import analyzor.modele.parties.SituationIso;

import java.util.List;

public interface CreerLabel {
    // labellise la base puis retourne toutes les situations ISO
    List<SituationIsoAvecRange> obtenirSituations(Situation situation, FormatSolution formatSolution);
}
