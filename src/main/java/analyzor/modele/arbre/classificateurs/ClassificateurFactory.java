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
     * @param situation
     * @return
     * @throws NonImplemente
     */
    public static Classificateur CreeClassificateur(Situation situation) throws NonImplemente {
        if (situation.getTour() == TourMain.Round.PREFLOP) {
            return new ClassificateurCumulatif();
        }
        else if (situation.getTour() == TourMain.Round.FLOP && ValeursConfig.SUBSETS) {
            if ((situation.getRang() == 0) || (situation.getRang() == 1 && ValeursConfig.SUBSETS_2E_RANK)) {
                return new ClassificateurSubset();
            }
        }

        return new ClassificateurDynamique();
    }
}
