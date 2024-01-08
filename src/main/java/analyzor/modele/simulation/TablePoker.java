package analyzor.modele.simulation;

import analyzor.modele.arbre.RecuperateurRange;
import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.config.ValeursConfig;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Action;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.Variante;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.vue.donnees.DTOSituation;

import java.util.*;

/**
 * interface entre le modèle et le controleur pour la gestion de la table de poker
 * récupère les actions possibles à partir de l'arbre abstrait et vérifie si elles existent dans la BDD
 * fait tourner le moteur de jeu et fournit les informations nécessaires au contrôleur
 * récupère les ranges et conserve leur statut
 */
public class TablePoker {
    private final LinkedList<SimuSituation> situations;
    private final HashMap<JoueurSimulation, RangeIso> rangesJoueurs;
    private final HashMap<SimuAction, RangeIso> rangeAction;
    private SimuSituation situationActuelle;
    private final MoteurJeu moteurJeu;
    private final CalculatriceEquite calculatriceEquite;
    private RecuperateurRange recuperateurRange;
    private JoueurSimulation joueurActuel;

    public TablePoker() {
        situations = new LinkedList<>();
        rangesJoueurs = new HashMap<>();
        moteurJeu = new MoteurJeu();
        rangeAction = new HashMap<>();

        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modePrecision();
        this.calculatriceEquite = new CalculatriceEquite(configCalculatrice);

        reset();
    }

    // méthodes utilisées par le controleur pour construire la table

    public void setFormatSolution(FormatSolution formatSolution) {
        reset();
        moteurJeu.reset(formatSolution);
        recuperateurRange = new RecuperateurRange(formatSolution);
    }

    // modification des joueurs

    public void setStack(JoueurSimulation joueurSimulation, float stack) {
        joueurSimulation.setStackDepart(stack);
    }

    public void setBounty(JoueurSimulation joueurSimulation, float bounty) {
        joueurSimulation.setBounty(bounty);
    }

    public void setHero(JoueurSimulation joueurSimulation, boolean hero) {
        joueurSimulation.setHero(hero);
    }

    // modification des situations

    public void setSituationSelectionnee(SimuSituation situation) {
        situation = situationActuelle;
        joueurActuel = situation.getJoueur();
        actualiserRanges();
        deselectionnerSituationsSuivantes(situation);
    }

    /**
     * le controleur appelera cette méthode quand il y aura un click sur une action
     */
    public void changerAction(SimuSituation situation, int indexAction) {
        moteurJeu.fixerAction(situation, indexAction);
        situation.fixerAction(indexAction);
    }

    /**
     * met l'action par défaut
     * appelé par le controleur
     * return null si l'action est déjà fixée, sinon l'index de la nouvelle action
     */
    public Integer fixerActionParDefaut(SimuSituation situation) {
        // on vérifie qu'une action n'est pas déjà fixée, si oui on retourne null
        if (situation.actionFixee()) {
            return null;
        }
        // sinon on change l'action et on retourne l'index de l'action
        else return situation.fixerActionParDefaut();
    }

    // construction de l'arbre

    /**
     * construit la suite de situations à partir de la situation fournie
     * si l'index est null va tout reconstruire de zéro
     * important, on ne fixe pas les actions dans cette procédure et on ne touche pas aux ranges
     */
    public LinkedList<SimuSituation> situationsSuivantes(SimuSituation situation) {
        // on supprime les situations à partir de l'index
        int indexSituation;
        if (situation == null) {
            situations.clear();
            indexSituation = 0;
        }
        else {
            indexSituation = situations.indexOf(situation);
            if (indexSituation == -1) throw new IllegalArgumentException("Situation non trouvée");
            int dernierIndex = situations.size();
            List<SimuSituation> situationsSupprimees = situations.subList(indexSituation, dernierIndex);
            situationsSupprimees.clear();
        }


        // on doit récupérer les ranges car on veut vérifier si elles existent
        // si null, on crée la première situation
        moteurJeu.setSituation(situation);

        while (moteurJeu.situationSuivante()) {
            SimuSituation simuSituation = moteurJeu.getSituation();
            if (trouverRanges(simuSituation)) {
                situations.add(simuSituation);
            }
            else break;
        }

        // retourne la suite d'actions possibles, incluant l'index
        return (LinkedList<SimuSituation>) situations.subList(indexSituation, situations.size());
    }

    // méthodes publiques utilisées par le contrôleur pour obtenir les infos

    /**
     * utilisé par le contrôleur au début
     * @return la liste tous les joueurs
     */
    public List<JoueurSimulation> getJoueurs() {
        return moteurJeu.getJoueurs();
    }

    /**
     * return l'équité du combo dans la situation actuellement sélectionnée
     */
    public float getEquite(String nomCombo) {
        Board board = new Board();
        List<RangeReelle> rangesVillains = new ArrayList<>();
        ComboReel comboReel = (new ComboIso(nomCombo)).toCombosReels().get(0);
        for (JoueurSimulation joueurSimulation : moteurJeu.getJoueurs()) {
            if (joueurSimulation == joueurActuel) continue;
            RangeIso rangeIso = rangesJoueurs.get(joueurSimulation);
            // range null veut dire que le joueur a fold
            if (rangeIso != null) {
                rangesVillains.add(new RangeReelle(rangeIso));
            }
        }

        return calculatriceEquite.equiteGlobaleMain(comboReel, board, rangesVillains);
    }

    // si indexAction est nul, on les veut toutes
    public LinkedHashMap<SimuAction, RangeIso> getRanges(Integer indexAction) {
        LinkedHashMap<SimuAction, RangeIso> ranges = new LinkedHashMap<>();
        for (SimuAction action : situationActuelle.getActions()) {
            if (indexAction == null || indexAction == action.getIndex()) {
                RangeIso rangeIso = rangeAction.get(action);
                ranges.put(action, rangeIso);
            }
        }

        return ranges;
    }

    // méthodes privées

    private void reset() {
        situations.clear();
        rangesJoueurs.clear();
    }

    /**
     * on actualise les ranges qui serviront pour le calcul d'équité
     */
    private void actualiserRanges() {
        // d'abord on remplit les ranges
        for (JoueurSimulation joueurSimulation : rangesJoueurs.keySet()) {
            RangeIso rangeIso = new RangeIso();
            rangeIso.remplir();
            rangesJoueurs.put(joueurSimulation, rangeIso);
        }

        // pour actualiser les ranges, il faut récupérer les actions antérieures
        // on est obligé de les recalculer à chaque fois car on l'user peut cliquer sur n'importe quelle action antérieure
        // par contre normalement, les ranges doivent avoir été déjà récupérées
        int indexSituation = situations.indexOf(situationActuelle);
        List<SimuSituation> situationsPrecedentes = situations.subList(0, indexSituation);
        for (SimuSituation situation : situationsPrecedentes) {
            JoueurSimulation joueurSituation = situation.getJoueur();
            RangeIso rangeJoueur = rangesJoueurs.get(joueurSituation);
            if (rangeJoueur == null) continue;

            SimuAction simuAction = situation.getActionSelectionnee();
            if (simuAction == null) throw new RuntimeException("Aucune action sélectionnée");

            // joueur foldé, on stocke null pour sa range
            if (simuAction.estFold()) {
                this.rangesJoueurs.put(joueurSituation, null);
                continue;
            }

            RangeIso rangeEnregistree = this.rangeAction.get(simuAction);
            if (rangeEnregistree == null) throw new RuntimeException("Aucune range trouvée pour l'action");

            rangeJoueur.multiplier(rangeEnregistree);
        }

    }

    private boolean trouverRanges(SimuSituation situation) {
        ProfilJoueur profilJoueur;
        if (situation.getJoueur().estHero()) {
            profilJoueur = ObjetUnique.profilJoueur(null, true);
        }
        else {
            profilJoueur = ObjetUnique.profilJoueur(null, false);
        }

        boolean rangeTrouvee = false;
        for (SimuAction action : situation.getActions()) {
            // si on a déjà récupéré la range on ne fait rien
            if (rangeAction.get(action) != null) continue;
            RangeSauvegardable rangeSauvegardee =
                    recuperateurRange.selectionnerRange(situation.getNoeudAbstrait().toLong(), situation.getStack(),
                            situation.getPot(), situation.getPotBounty(), action.getBetSize(), profilJoueur, true);
            // si il n'y a pas de range, l'action n'existe pas
            if (rangeSauvegardee == null) continue;
            if (!(rangeSauvegardee instanceof RangeIso))
                throw new RuntimeException("Pour l'instant, on ne travaille que avec range Iso");

            RangeIso rangeIso = (RangeIso) rangeSauvegardee;
            rangeAction.put(action, rangeIso);
            rangeTrouvee = true;
        }

        return rangeTrouvee;
    }

    private void deselectionnerSituationsSuivantes(SimuSituation situation) {
        // on désélectionne les actions suivantes
        int indexSituation = situations.indexOf(situation);
        if (indexSituation == -1) throw new IllegalArgumentException("Situation non trouvée");
        List<SimuSituation> situationsSuivantes = situations.subList(indexSituation + 1, situations.size());
        for (SimuSituation simuSituation : situationsSuivantes) {
            simuSituation.deselectionnerAction();
        }
    }

}
