package analyzor.modele.simulation;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.parties.Variante;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * en interaction avec l'abre abstrait
 * simule le jeu de poker, détermine quel joueur doit jouer
 * fixe le nom des positions
 * garde les traces des stacks
 * peut reconstruire l'arbre à partir de n'importe quelle situation
 */
class MoteurJeu {
    private final static HashMap<Integer, String[]> nomsPosition;

    static {
        nomsPosition = new HashMap<>();
        nomsPosition.put(3, new String[]{"BTN", "SB", "BB"});
        nomsPosition.put(6, new String[]{"MP", "HJ", "CO", "BTN", "SB", "BB"});
    }
    private final List<JoueurSimulation> joueurs;
    private final HashMap<JoueurSimulation, Float> stacks;
    private ArbreAbstrait arbreAbstrait;
    private HashMap<SimuAction, SimuSituation> prochainesSituations;
    private JoueurSimulation joueurActuel;

    MoteurJeu() {
        joueurs = new ArrayList<>();
        stacks = new HashMap<>();
    }

    // interface de controle par TablePoker

    void reset(FormatSolution formatSolution) {
        arbreAbstrait = new ArbreAbstrait(formatSolution);
        initialiserJoueurs(formatSolution);
    }

    void fixerAction(SimuSituation situation, Integer indexAction) {

    }

    // utilisé pour construire la situation suivante avec une action par défaut
    // return null si pas d'action suivante
    boolean situationSuivante() {
        // todo important : on affiche BB que si il y a eu une action avant car sinon SB peut être leaf
        // on considère que l'action suivante est toujours un fold pour calculer l'arbre suivant
        return false;
    }

    // si null on crée la première situation
    void setSituation(SimuSituation situation) {
    }


    // utilisé par TablePoker pour obtenir les infos

    SimuSituation getSituation() {
        return null;
    }

    List<JoueurSimulation> getJoueurs() {
        return new ArrayList<>();
    }

    JoueurSimulation getJoueurActuel() {
        return joueurActuel;
    }


    // méthodes privées

    /**
     * on va initialiser les joueurs, avec un stack et un bounty "standard"
     */
    private void initialiserJoueurs(FormatSolution formatSolution) {
        for (int i = 0; i < formatSolution.getNombreJoueurs(); i++) {
            JoueurSimulation nouveauJoueur =
                    new JoueurSimulation(i, nomsPosition.get(formatSolution.getNombreJoueurs())[i]);
            // todo : est ce que on fait un autre stack pour CASH GAME ?
            if (formatSolution.getNomFormat() == Variante.PokerFormat.SPIN) {
                nouveauJoueur.setStackDepart(25);
            }
            else {
                nouveauJoueur.setStackDepart(100);
            }
            if (formatSolution.getKO()) {
                nouveauJoueur.setBounty((float) formatSolution.getMinBuyIn() / 2);
            }
            else {
                nouveauJoueur.setBounty(null);
            }
            joueurs.add(nouveauJoueur);
        }
    }
}
