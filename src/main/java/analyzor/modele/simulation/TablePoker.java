package analyzor.modele.simulation;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Action;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.poker.RangeReelle;

import java.util.*;

public class TablePoker {
    private static String[] nomsPosition = {"MP", "HJ", "CO", "BTN", "SB", "BB"};
    private List<JoueurSimulation> joueurs = new ArrayList<>();
    private List<Action> situations = new ArrayList<>();
    private FormatSolution formatSolution;
    private HashMap<JoueurSimulation, RangeIso> rangesJoueurs;

    public TablePoker() {
        reset();
    }

    // méthodes utilisées par le controleur pour construire la table

    public void setFormatSolution(FormatSolution formatSolution) {
        reset();
        this.formatSolution = formatSolution;
        initialiserJoueurs();
        remplissageSituations();
    }

    public void setStack(int idJoueur, float stack) {

    }

    public void setBounty(int idJoueur, float bounty) {

    }

    public void setHero(int idJoueur) {

    }

    public void changerAction(int indexSituation, int indexAction) {
        // on supprime toutes les actions suivantes, on fixe chaque situation sur fixée
        // on recalcule les situations
        this.remplissageSituations();
        this.actualiserRanges();
    }

    // méthodes publiques utilisées par le contrôleur pour obtenir les infos
    public List<JoueurSimulation> getJoueurs() {
        return joueurs;
    }

    public List<SimuSituation> situationsSuivantes(int indexSituation) {
        //retourne la suite d'actions possibles
        return null;
    }

    public float getEquite(int indexSituation, RangeCondensee.ComboCondense comboCondense) {
        return 0f;
    }

    // méthodes privées

    /**
     * on va initialiser les joueurs, avec un stack et un bounty "standard"
     */
    private void initialiserJoueurs() {
        for (int i = 0; i < formatSolution.getNombreJoueurs(); i++) {
            JoueurSimulation nouveauJoueur = new JoueurSimulation(i, nomsPosition[i]);
            // todo : il faudrait connaitre stack et bounty de départ...
            nouveauJoueur.setStack(25);
            if (formatSolution.getKO()) {
                nouveauJoueur.setBounty((float) formatSolution.getMinBuyIn() / 2);
            }
            else {
                nouveauJoueur.setBounty(null);
            }
        }
    }

    private void reset() {
        joueurs.clear();
    }

    private void remplissageSituations() {
        // jusqu->à leaf, on va faire que des FOLD (ou CALL si FOLD pas possible)

    }

    private void actualiserRanges() {
    }

}
