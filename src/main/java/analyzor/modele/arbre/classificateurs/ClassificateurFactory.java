package analyzor.modele.arbre.classificateurs;

import analyzor.modele.config.ValeursConfig;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.Situation;
import analyzor.modele.parties.TourMain;

/**
 * Implémentation du Pattern Factory Builder
 */
public class ClassificateurFactory {

    public static Classificateur CreeClassificateur() {
        return new ClassificateurCumulatif();
    }

    /**
     * définit le classificateur adapté selon critères modifiables
     * @return
     * @throws NonImplemente
     */
    public static Classificateur creeClassificateur(TourMain.Round round, int rangAction) throws NonImplemente {
        if (round == TourMain.Round.PREFLOP) {
            return new ClassificateurCumulatif();
        }
        else if (round == TourMain.Round.FLOP && ValeursConfig.SUBSETS) {
            if ((rangAction == 0) || (rangAction == 1 && ValeursConfig.SUBSETS_2E_RANK)) {
                return new ClassificateurSubset();
            }
        }

        return new ClassificateurDynamique();
    }
}
