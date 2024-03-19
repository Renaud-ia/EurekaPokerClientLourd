package analyzor.modele.extraction.winamax;

import analyzor.modele.extraction.InterpreteurPartie;
import analyzor.modele.parties.TourMain;

public class InterpreteurPartieWinamax implements InterpreteurPartie {
    /*
       lit le début des lignes, comprend la structure du fichier
       et indique au lecteur ce qu'il doit chercher dans chaque ligne
    */

    private enum EndroitFichier {
        NOUVELLE_MAIN,
        INFOS_TABLE,
        POSITION_JOUEURS,
        JOUEURS_TROUVES,
        BLINDES_ANTE,
        CARTES_HERO,
        NOUVEAU_TOUR,
        ACTION,
        SHOWDOWN,
        GAINS,
        POT_CASH_GAME,
        FIN_MAIN,
        NON_CHERCHE
    }

    private TourMain.Round tourActuel;

    private EndroitFichier endroitActuel;


    protected  InterpreteurPartieWinamax() {
        tourActuel = TourMain.Round.PREFLOP;
        endroitActuel = EndroitFichier.NOUVELLE_MAIN;
    }

    /**
     * vérifie le début des lignes et l'enchainement de la structure pour déterminer où on se trouve dans le fichier
     */
    @Override
    public void lireLigne(String ligne) {

        if (ligne.startsWith("Winamax Poker")) {
            endroitActuel = EndroitFichier.NOUVELLE_MAIN;
            tourActuel = TourMain.Round.PREFLOP;
        }

        else if (ligne.startsWith("Table")) {
            endroitActuel = EndroitFichier.INFOS_TABLE;
        }


        else if (ligne.startsWith("Seat")) {
            if (ligne.contains(" won ")) {
                endroitActuel = EndroitFichier.GAINS;
            }
            else if (ligne.contains(" lost ")) {
                endroitActuel = EndroitFichier.NON_CHERCHE;
            }
            else {
                endroitActuel = EndroitFichier.POSITION_JOUEURS;
            }
        }

        else if (ligne.startsWith("***")) {
            if (ligne.startsWith("*** SUMMARY ***")) {
                endroitActuel = EndroitFichier.FIN_MAIN;
                tourActuel = null;
            }
            else if (ligne.startsWith("*** SHOW DOWN ***")) {
                endroitActuel = EndroitFichier.SHOWDOWN;
                tourActuel = null;
            }

            else if (ligne.startsWith("*** ANTE/BLINDS ***")) {
                endroitActuel = EndroitFichier.JOUEURS_TROUVES;
            }
            else if (endroitActuel == EndroitFichier.CARTES_HERO || endroitActuel == EndroitFichier.BLINDES_ANTE) {
                tourActuel = TourMain.Round.PREFLOP;
                endroitActuel = EndroitFichier.NOUVEAU_TOUR;
            }

            else {
                tourActuel = tourActuel.suivant();
                endroitActuel = EndroitFichier.NOUVEAU_TOUR;
            }
        }

        else if (endroitActuel == EndroitFichier.NOUVEAU_TOUR || endroitActuel == EndroitFichier.ACTION) {
            if (ligne.contains("collected") || ligne.contains("shows")) {
                endroitActuel = EndroitFichier.NON_CHERCHE;
            }
            else {
                endroitActuel = EndroitFichier.ACTION;
            }
        }

        else if (endroitActuel == EndroitFichier.JOUEURS_TROUVES || endroitActuel == EndroitFichier.BLINDES_ANTE) {
            if (ligne.startsWith("Dealt to")) {
                endroitActuel = EndroitFichier.CARTES_HERO;
            }
            else {
                endroitActuel = EndroitFichier.BLINDES_ANTE;
            }
        }

        else if (ligne.startsWith("Total pot")) {
            endroitActuel = EndroitFichier.POT_CASH_GAME;
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
        return endroitActuel == EndroitFichier.BLINDES_ANTE;
    }

    @Override
    public boolean nouveauTour() {
        return endroitActuel == EndroitFichier.NOUVEAU_TOUR;
    }

    @Override
    public boolean pasPreflop() {
        return tourActuel != TourMain.Round.PREFLOP;
    }

    @Override
    public boolean cartesHeroCherchees() { return endroitActuel == EndroitFichier.CARTES_HERO; }

    @Override
    public boolean showdownTrouve() { return endroitActuel == EndroitFichier.SHOWDOWN; }

    @Override
    public boolean infosTable() {
        return endroitActuel == EndroitFichier.INFOS_TABLE;
    }

    @Override
    public boolean mainFinie() {
        return endroitActuel == EndroitFichier.FIN_MAIN;
    }

    @Override
    public boolean potTrouveCashGame() {
        return endroitActuel == EndroitFichier.POT_CASH_GAME;
    }
}
