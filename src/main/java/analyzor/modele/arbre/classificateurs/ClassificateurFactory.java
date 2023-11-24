package analyzor.modele.arbre.classificateurs;

import analyzor.modele.config.ValeursConfig;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.TourMain;

/**
 * Implémentation du Pattern Factory Builder
 */
public class ClassificateurFactory {

    /**
     * définit le classificateur adapté selon critères modifiables
     * @return
     * @throws NonImplemente
     */
    public static Classificateur creeClassificateur(TourMain.Round round, int rangAction, FormatSolution formatSolution)
            throws NonImplemente {
        if (round == TourMain.Round.PREFLOP) {
            return new ClassificateurCumulatif(formatSolution);
        }
        else if (round == TourMain.Round.FLOP && ValeursConfig.SUBSETS) {
            if ((rangAction == 0) || (rangAction == 1 && ValeursConfig.SUBSETS_2E_RANK)) {
                return new ClassificateurSubset(formatSolution);
            }
            // todo : à retirer quand on intégrera classificateur dynamique
            else if(rangAction == 2 && ValeursConfig.SUBSETS_2E_RANK) {
                return null;
            }
        }

        return new ClassificateurDynamique(formatSolution);
    }
}
