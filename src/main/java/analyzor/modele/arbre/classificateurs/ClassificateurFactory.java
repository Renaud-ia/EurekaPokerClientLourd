package analyzor.modele.arbre.classificateurs;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.TourMain;


public class ClassificateurFactory {

    
    public static Classificateur creeClassificateur(
            TourMain.Round round, int rangAction, FormatSolution formatSolution, ProfilJoueur profilJoueur)
            throws NonImplemente {
        if (round == TourMain.Round.PREFLOP) {
            return new ClassificateurCumulatif(formatSolution, profilJoueur);
        }
        else if (round == TourMain.Round.FLOP) {
            return new ClassificateurSubset(formatSolution);
        }

        return new ClassificateurDynamique(formatSolution);
    }
}
