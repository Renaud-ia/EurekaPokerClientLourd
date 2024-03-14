package analyzor.modele.simulation;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;

import java.util.*;

/**
 * interface entre le modèle et le controleur pour la gestion de la table de poker
 * récupère les situations depuis MoteurJeu
 * fait tourner le moteur de jeu et fournit les informations nécessaires au contrôleur
 * récupère les ranges et conserve leur statut
 */
public class TableSimulation {
    private LinkedList<SimuSituation> situations;
    private final HashMap<TablePoker.JoueurTable, RangeIso> rangesJoueurs;
    private final HashMap<SimuAction, RangeIso> rangeAction;
    private SimuSituation situationActuelle;
    private final MoteurJeu moteurJeu;
    private TablePoker.JoueurTable joueurActuel;
    private boolean leafTrouvee;

    public TableSimulation() {
        situations = new LinkedList<>();
        rangesJoueurs = new HashMap<>();
        moteurJeu = new MoteurJeu();
        rangeAction = new HashMap<>();

        reset();
    }

    // méthodes utilisées par le controleur pour construire la table

    public void setFormatSolution(FormatSolution formatSolution) {
        reset();
        moteurJeu.reset(formatSolution);
    }

    public boolean reconstruireSituations() {
        reset();
        return moteurJeu.resetSituations();
    }

    private void reset() {
        situations.clear();
        rangesJoueurs.clear();
    }


    // modification des joueurs

    public void resetJoueur(TablePoker.JoueurTable joueurModele) {
        joueurModele.reset();
    }

    public void setStack(TablePoker.JoueurTable joueurSimulation, float stack) {
        joueurSimulation.setStackDepart(stack);
    }

    public void setBounty(TablePoker.JoueurTable joueurSimulation, float bounty) {
        joueurSimulation.setBounty(bounty);
    }

    public void setHero(TablePoker.JoueurTable joueurSimulation, boolean hero) {
        joueurSimulation.setHero(hero);
    }

    // modification des situations par le controleur

    public void setSituationSelectionnee(SimuSituation situation) {
        if (!situations.contains(situation)) throw new IllegalArgumentException("SITUATION NON TROUVEE");
        situationActuelle = situation;
        joueurActuel = situation.getJoueur();

        // on veut actualiser les ranges à chaque fois
        actualiserRanges();
    }

    /**
     * le controleur appelera cette méthode quand il y aura un click sur une action
     */
    public Integer changerAction(SimuSituation situation, Integer indexAction) {
        return moteurJeu.fixerAction(situation, indexAction);
    }

    /**
     * met l'action par défaut
     * appelé par le controleur
     * return null si l'action est déjà fixée, sinon l'index de la nouvelle action
     */
    public Integer fixerActionParDefaut(SimuSituation situation) {
        // on vérifie qu'une action n'est pas déjà fixée, si oui on retourne null
        Integer actionFixee = situation.actionFixee();
        if (actionFixee != null) {
            return actionFixee;
        }
        // sinon on change l'action et on retourne l'index de l'action
        else return changerAction(situation, null);
    }

    // construction de l'arbre

    /**
     * récupère la liste des situations depuis le MoteurJeu
     * puis retourne la sous-liste depuis l'index
     * si l'index est null va tout reconstruire de zéro
     * important, on ne fixe pas les actions dans cette procédure et on ne touche pas aux ranges
     */
    public LinkedList<SimuSituation> situationsSuivantes(SimuSituation situation) {
        System.out.println("SITUATION DEMANDEE DEBUT : " + situations);

        // on récupère l'index de la situation demandée
        int indexSituation;
        if (situation == null) {
            indexSituation = 0;
        }
        else {
            indexSituation = situations.indexOf(situation);
            if (indexSituation == -1) {
                throw new RuntimeException("Situation non trouvée");
            }
        }

        // on réactualise les situations
        situations = new LinkedList<>(moteurJeu.getSuiteSituations());
        leafTrouvee = moteurJeu.leafTrouvee();

        System.out.println("SITUATION DEMANDEE FIN : " + situations);

        // retourne la suite d'actions possibles, incluant l'index
        return new LinkedList<>(situations.subList(indexSituation, situations.size()));
    }

    // méthodes publiques utilisées par le contrôleur pour obtenir les infos

    /**
     * utilisé par le contrôleur au début
     * @return la liste tous les joueurs
     */
    public LinkedList<TablePoker.JoueurTable> getJoueurs() {
        return moteurJeu.getJoueursSimulation();
    }

    public List<RangeReelle> getRangesVillains() {
        List<RangeReelle> rangesVillains = new ArrayList<>();
        for (TablePoker.JoueurTable joueurSimulation : moteurJeu.getJoueursSimulation()) {
            if (joueurSimulation == joueurActuel) continue;
            RangeIso rangeIso = rangesJoueurs.get(joueurSimulation);
            // range null veut dire que le joueur a fold
            if (rangeIso != null) {
                rangesVillains.add(new RangeReelle(rangeIso));
            }
        }

        return rangesVillains;
    }

    /**
     * méthode appelée par le controleur pour récupérer les ranges dans l'état où elle sont
     * retourne une liste vide si problème de de récupération des ranges
     * @param indexAction
     * @return une liste de ranges par action
     */
    public LinkedHashMap<SimuAction, RangeIso> getRangesSituationActuelle(Integer indexAction) {
        LinkedHashMap<SimuAction, RangeIso> ranges = new LinkedHashMap<>();
        // d'abord on récupère la range actuelle du joueur
        RangeIso rangeTotale = rangesJoueurs.get(situationActuelle.getJoueur());
        if (rangeTotale == null) {
            throw new RuntimeException("Range non trouvé pour joueur actuel");
        }

        for (SimuAction action : situationActuelle.getActions()) {
            // si indexAction est nul, on les veut toutes
            if (indexAction == null || indexAction == action.getIndex()) {
                RangeIso rangeAvantAction = rangeTotale.copie();
                RangeIso rangeRelativeAction = (RangeIso) action.getRange();
                if (rangeRelativeAction == null) return new LinkedHashMap<>();
                rangeAvantAction.multiplier(rangeRelativeAction);
                ranges.put(action, rangeAvantAction);
            }
        }

        return ranges;
    }

    // méthodes privées

    /**
     * on actualise les ranges qui serviront pour le calcul d'équité et pour multiplier les ranges
     * appelé à chaque fois qu'on a besoin des ranges
     */
    private void actualiserRanges() {
        // d'abord on remplit les ranges
        for (TablePoker.JoueurTable joueurSimulation : moteurJeu.getJoueursSimulation()) {
            RangeIso rangeIso = new RangeIso();
            rangeIso.remplir();
            rangesJoueurs.put(joueurSimulation, rangeIso);
        }

        // pour actualiser les ranges, il faut récupérer les actions antérieures
        // on est obligé de les recalculer à chaque fois car on l'user peut cliquer sur n'importe quelle action antérieure
        // par contre normalement, les ranges doivent avoir été déjà récupérées
        int indexSituation = situations.indexOf(situationActuelle);
        if (indexSituation == -1) throw new RuntimeException("Situation non trouvée");

        List<SimuSituation> situationsPrecedentes = situations.subList(0, indexSituation);
        for (SimuSituation situation : situationsPrecedentes) {
            TablePoker.JoueurTable joueurSituation = situation.getJoueur();
            RangeIso rangeJoueur = rangesJoueurs.get(joueurSituation);
            if (rangeJoueur == null) continue;

            SimuAction simuAction = situation.getActionActuelle();
            if (simuAction == null) throw new RuntimeException("Aucune action sélectionnée");

            // joueur foldé, on stocke null pour sa range
            if (simuAction.estFold()) {
                this.rangesJoueurs.put(joueurSituation, null);
                continue;
            }

            RangeIso rangeEnregistree = (RangeIso) simuAction.getRange();
            if (rangeEnregistree == null) throw new RuntimeException("Aucune range trouvée pour l'action");

            rangeJoueur.multiplier(rangeEnregistree);
        }

    }

    public boolean leafTrouvee() {
        return leafTrouvee;
    }

    public void modeHU(FormatSolution formatSolution, boolean modeHU) {
        moteurJeu.initialiserJoueurs(formatSolution, modeHU);
    }


}
