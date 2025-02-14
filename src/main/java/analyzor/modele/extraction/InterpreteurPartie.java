package analyzor.modele.extraction;

import analyzor.modele.parties.TourMain;

public interface InterpreteurPartie {
    /*
    interface pour comprende la structure d'un fichier
    indique ce qu'il faut chercher dans chaque ligne
     */
    public void lireLigne(String ligne);
    public boolean nouvelleMain();
    public boolean joueurCherche();
    public boolean nouveauTour();
    public boolean pasPreflop();
    public TourMain.Round nomTour();
    public boolean actionCherchee();
    public boolean gainCherche();
    public boolean blindesAntesCherchees();
    public boolean cartesHeroCherchees();
    public boolean showdownTrouve();
    boolean infosTable();
    boolean mainFinie();
    boolean potTrouveCashGame();
}
