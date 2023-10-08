package analyzor.modele.extraction.winamax;

import analyzor.modele.extraction.InterpreteurPartie;
import analyzor.modele.parties.TourMain;

public class InterpreteurPartieWinamax implements InterpreteurPartie {
    /*
        comprend la structure du fichier et indique au lecteur ce qu'il doit chercher dans chaque ligne
         */

    private enum EndroitFichier {
        NOUVELLE_MAIN,
        POSITION_JOUEURS,
        NOUVEAU_TOUR,
        ACTION,
        GAINS,
        NON_CHERCHE
    }

    private TourMain.Round tourActuel;

    private EndroitFichier endroitActuel;


    protected  InterpreteurPartieWinamax() {
        tourActuel = TourMain.Round.PREFLOP;
        endroitActuel = EndroitFichier.NOUVEAU_TOUR;
    }

    @Override
    public void lireLigne(String ligne) {
        /*
        vérifie le début des lignes et l'enchainement de la structure pour déterminer où on se trouve dans le fichier
         */
        if (ligne.startsWith("Winamax Poker")) {
            endroitActuel = EndroitFichier.NOUVEAU_TOUR;
        }

        else if (ligne.startsWith("Seat")) {
            if (endroitActuel == EndroitFichier.NOUVEAU_TOUR) {
                endroitActuel = EndroitFichier.POSITION_JOUEURS;
            }
            else {
                endroitActuel = EndroitFichier.GAINS;
            }
        }

        else if (ligne.startsWith("***")) {
            if (endroitActuel == EndroitFichier.POSITION_JOUEURS) {
                tourActuel = TourMain.Round.PREFLOP;
                endroitActuel = EndroitFichier.NOUVEAU_TOUR;
            }

            else if (tourActuel != TourMain.Round.RIVER) {
                tourActuel = tourActuel.suivant();
                endroitActuel = EndroitFichier.NOUVEAU_TOUR;
            }

            else {
                endroitActuel = EndroitFichier.NON_CHERCHE;
            }
        }

        else if (endroitActuel == EndroitFichier.NOUVEAU_TOUR) {
            // après un nouveau tour, on cherche forcément une action
            endroitActuel = EndroitFichier.ACTION;
        }

        else {
            endroitActuel = EndroitFichier.NON_CHERCHE;
        }
    }

    @Override
    public boolean nouvelleMain() {
        return endroitActuel == EndroitFichier.NOUVELLE_MAIN;
    }

    @Override
    public boolean joueurCherche() {
        return endroitActuel == EndroitFichier.POSITION_JOUEURS;
    }

    @Override
    public TourMain.Round nomTour() {
        return tourActuel;
    }

    @Override
    public boolean actionCherchee() {
        return endroitActuel == EndroitFichier.ACTION;
    }

    @Override
    public boolean gainCherche() {
        return endroitActuel == EndroitFichier.GAINS;
    }

    @Override
    public boolean blindesAntesCherchees() {
        return endroitActuel == EndroitFichier.POSITION_JOUEURS;
    }

    @Override
    public boolean nouveauTour() {
        return endroitActuel == EndroitFichier.NOUVEAU_TOUR;
    }

    @Override
    public boolean pasPreflop() {
        return tourActuel != TourMain.Round.PREFLOP;
    }
}
