package analyzor.modele.simulation;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Action;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.poker.RangeReelle;

import java.util.*;

public class TablePoker {
    private final static String[] nomsPosition = {"MP", "HJ", "CO", "BTN", "SB", "BB"};
    private final List<JoueurSimulation> joueurs;
    private final List<Action> situations;
    private FormatSolution formatSolution;
    private final HashMap<JoueurSimulation, RangeIso> rangesJoueurs;
    private HashMap<SimuAction, SimuSituation> prochainesSituations;

    public TablePoker() {
        joueurs = new ArrayList<>();
        situations = new ArrayList<>();
        rangesJoueurs = new HashMap<>();
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

    public void setHero(int idJoueur, boolean hero) {

    }

    public void changerAction(int indexSituation, int indexAction) {
        // on supprime toutes les actions suivantes, on fixe chaque situation sur fixée
        // on recalcule les situations
        this.remplissageSituations();
        // on actualise les ranges
        this.actualiserRanges();
    }

    // méthodes publiques utilisées par le contrôleur pour obtenir les infos
    public List<JoueurSimulation> getJoueurs() {
        return joueurs;
    }

    /**
     * construit la suite de situations à partir de l'index fourni
     * si l'index est null va tout reconstruire de zéro
     */
    public List<SimuSituation> situationsSuivantes(Integer indexSituation) {
        //retourne la suite d'actions possibles, incluant l'index
        // todo important : on affiche BB que si il y a eu une action avant car sinon SB peut être leaf
        // on considère que l'action suivante est toujours un fold pour calculer l'arbre suivant
        return null;
    }

    /**
     * met l'action par défaut
     * actualise la range du joueur
     */
    public Integer fixerActionParDefaut(int indexModele) {
        return null;
    }

    public float getEquite(String nomCombo) {
        return 0f;
    }

    // si indexAction est nulle, on les veut toutes
    public HashMap<SimuAction, RangeIso> getRanges(Integer indexAction) {
        return null;
    }

    // méthodes privées

    /**
     * on va initialiser les joueurs, avec un stack et un bounty "standard"
     */
    private void initialiserJoueurs() {
        for (int i = 0; i < formatSolution.getNombreJoueurs(); i++) {
            JoueurSimulation nouveauJoueur = new JoueurSimulation(i, nomsPosition[i]);
            // todo : il faudrait connaitre stack de départ...
            nouveauJoueur.setStack(25);
            if (formatSolution.getKO()) {
                nouveauJoueur.setBounty((float) formatSolution.getMinBuyIn() / 2);
            }
            else {
                nouveauJoueur.setBounty(null);
            }
            joueurs.add(nouveauJoueur);
        }
    }

    private void reset() {
        joueurs.clear();
        situations.clear();
        rangesJoueurs.clear();
    }

    private void remplissageSituations() {
        // jusqu->à leaf, on va faire que des FOLD (ou CALL si FOLD pas possible)

    }

    private void actualiserRanges() {
    }

    public void setSituationSelectionnee(int indexModele) {
    }
}
