package analyzor.modele.simulation;

import analyzor.modele.arbre.RecuperateurRange;
import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.arbre.noeuds.NoeudSituation;
import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import analyzor.modele.poker.RangeSauvegardable;

import java.util.*;

/**
 * simule le jeu de poker, détermine quel joueur doit jouer
 * en interaction avec l'abre abstrait et récupérateur Range, vérifie que les situations et les actions existent
 * crée les situations
 * fixe le nom des positions
 * garde les traces des stacks
 * est responsable de la hiérarchie des actions
 * todo : la classe peut probablement être refactorisée entre premièreSituation et créerSituation
 * todo : certaines parties font doublon avec EnregistreurPartie, il faudrait refactoriser
 */
class MoteurJeu {
    // on stocke les informations immuables, le reste est dans les situations
    private HashMap<Integer, String> nomsPosition;
    private final HashMap<Integer, JoueurSimulation> joueurs;
    private final HashMap<JoueurSimulation, Integer> positionsJoueurs;
    private final HashMap<JoueurSimulation, Float> stacksDeparts;
    private final LinkedList<SimuSituation> situationsActuelles;
    private final HashMap<SimuAction, SimuSituation> situationsSuivantes;
    private RecuperateurRange recuperateurRange;
    private FormatSolution formatSolution;

    MoteurJeu() {
        joueurs = new HashMap<>();
        positionsJoueurs = new HashMap<>();
        stacksDeparts = new HashMap<>();
        situationsActuelles = new LinkedList<>();
        situationsSuivantes = new HashMap<>();
    }

    // interface de controle par TablePoker

    void reset(FormatSolution formatSolution) {
        this.formatSolution = formatSolution;
        recuperateurRange = new RecuperateurRange(formatSolution);
        nomsPosition = NomsPositions.obtNoms(formatSolution.getNombreJoueurs());
        initialiserJoueurs(formatSolution);
        initialiserSituations();
    }

    int fixerAction(SimuSituation situation, Integer indexAction) {
        // si null action par défaut => on ne change rien au reste
        if (indexAction == null) {
            indexAction = situation.fixerActionParDefaut();
        }

        // sinon action imposée => on doit reconstruire la suite
        else {
            situation.fixerAction(indexAction);
            construireSuiteSituations(situation);
        }

        return indexAction;
    }

    // récupération des infos par TablePoker

    Set<JoueurSimulation> getJoueurs() {
        return new HashSet<>(joueurs.values());
    }

    /**
     * renvoie toutes les situations actuelles
     */
    public LinkedList<SimuSituation> getSuiteSituations() {
        return situationsActuelles;
    }


    // méthodes privées

    private void initialiserSituations() {
        // todo : on pourrait garder une HashMap au cas où on revient en arrière mais galère pour bénéfice limité
        situationsSuivantes.clear();
        situationsActuelles.clear();
        SimuSituation premiereSituation = premiereSituation();
        remplirSituation(premiereSituation);
        premiereSituation.fixerActionParDefaut();

        situationsActuelles.add(premiereSituation);
        construireSuiteSituations(premiereSituation);
    }

    private SimuSituation premiereSituation() {
        // todo refactoriser avec creerSituation => c'est un peu le bordel
        stacksDeparts.clear();
        // il faut distinguer un joueur foldé et un joueur dont le stack est à 0 (ce qui est facile à savoir)
        HashMap<JoueurSimulation, Boolean> joueurFolde = new HashMap<>();
        HashMap<JoueurSimulation, Float> stacksApresBlindes = new HashMap<>();

        float pot = 0;
        float potBounty = 0;
        // on initialise les stacks de départ, on pose les blindes et ante et on calcule le potBounty
        for (int i = 0; i < joueurs.size(); i++) {
            JoueurSimulation joueurTraite = joueurs.get(i);
            if (joueurTraite == null) throw new RuntimeException("Joueur non trouvé pour index : " + i);

            float stackActuel = joueurTraite.getStackDepart();
            stacksDeparts.put(joueurTraite, stackActuel);
            if (formatSolution.getAnte()) {
                float valeurAnte = 0.15f;
                stackActuel -= valeurAnte;
                pot += valeurAnte;
            }

            float blindePosee = 0;
            // le joueur est en sb
            if (i == joueurs.size() - 2) {
                blindePosee += Math.max(stackActuel, 0.5f);
            }
            // le joueur est en bb
            else if (i == joueurs.size() - 1) {
                blindePosee += Math.max(stackActuel, 1f);
            }

            stackActuel -= blindePosee;
            pot += blindePosee;

            stacksApresBlindes.put(joueurTraite, stackActuel);

            if (formatSolution.getKO()) {
                potBounty += (joueurTraite.getStackDepart() - stackActuel) * joueurTraite.getBounty()
                        / joueurTraite.getStackDepart();
            }

            joueurFolde.put(joueurTraite, false);
        }

        JoueurSimulation joueurInitial = joueurs.get(0);
        NoeudAbstrait premierNoeud = new NoeudAbstrait(joueurs.size(), TourMain.Round.PREFLOP);

        ProfilJoueur profilJoueur;
        if (joueurInitial.estHero()) {
            profilJoueur = ObjetUnique.selectionnerHero();
        }
        else {
            profilJoueur = ObjetUnique.selectionnerVillain();
        }

        float stackEffectif = calculerStackEffectif(joueurInitial, stacksApresBlindes, joueurFolde);

        NoeudSituation noeudInitial =
                recuperateurRange.noeudSituationPlusProche(
                        premierNoeud.toLong(), stackEffectif, pot, potBounty, profilJoueur);

        return new SimuSituation(noeudInitial, joueurInitial, stacksApresBlindes, joueurFolde, pot, potBounty);
    }

    private void construireSuiteSituations(SimuSituation situation) {
        int indexSituation = situationsActuelles.indexOf(situation);
        if (indexSituation == -1) throw new IllegalArgumentException("Situation non trouvée");

        // on fixe les actions par défaut des situations + 1
        // puis on les supprime
        for (int i = situationsActuelles.size() - 1; i > indexSituation; i--) {
            SimuSituation simuSituation = situationsActuelles.get(i);
            simuSituation.deselectionnerAction();
            simuSituation.fixerActionParDefaut();
            situationsActuelles.remove(i);
        }

        SimuAction action = situation.getActionActuelle();
        // tant qu'on arrive à créer un noeud, on fixe une action par défaut et on construit la situation suivante
        while ((situation = creerSituation(action, situation)) != null) {
            remplirSituation(situation);
            situationsActuelles.add(situation);
            situation.deselectionnerAction();
            situation.fixerActionParDefaut();
            action = situation.getActionActuelle();
        }
    }

    /**
     * crée un noeud situation à partir d'un id de Noeud
     * va vérifier qu'il existe dans la base
     * et enregistrer les bonnes valeurs (joueurs, stacks, etc.)
     * @return la simuSituation, null si on a pas trouvé dans la BDD
     */
    private SimuSituation creerSituation(SimuAction action, SimuSituation situation) {
        if (action == null || situation == null) {
            throw new IllegalArgumentException("L'action et/ou situation est nulle");
        }

        // on vérifie qu'on l'a pas déjà récupérée
        SimuSituation situationDejaRecuperee = situationsSuivantes.get(action);
        if (situationDejaRecuperee != null) {
            return situationDejaRecuperee;
        }

        // on récupère les infos
        JoueurSimulation joueurPrecedent = situation.getJoueur();
        HashMap<JoueurSimulation, Float> stacksApresAction = new HashMap<>(situation.getStacks());
        HashMap<JoueurSimulation, Boolean> joueurFolde = new HashMap<>(situation.getJoueurFolde());

        // on met à jour les infos selon l'action sélectionnée
        JoueurSimulation joueurSuivant = joueurSuivant(joueurPrecedent, joueurs, stacksApresAction, joueurFolde);
        stacksApresAction.put(joueurSuivant, stacksApresAction.get(joueurSuivant) - action.getBetSize());
        float stackEffectif = calculerStackEffectif(situation.getJoueur(), stacksApresAction, joueurFolde);
        float pot = situation.getPot() + action.getBetSize();
        float potBounty = calculerPotBounty(stacksDeparts, stacksApresAction, joueurFolde);
        if (action.estFold()) {
            joueurFolde.put(joueurSuivant, true);
        }

        ProfilJoueur profilJoueur;
        if (joueurSuivant.estHero()) {
            profilJoueur = ObjetUnique.selectionnerHero();
        }
        else {
            profilJoueur = ObjetUnique.selectionnerVillain();
        }

        // on vérifie qu'on trouve une situation correspondante dans la BDD
        NoeudSituation noeudSuivant =
                recuperateurRange.noeudSituationPlusProche(
                        action.getIdNoeud(), stackEffectif, situation.getPot(), situation.getPotBounty(), profilJoueur);

        // on a rien trouvé dans la base on s'arrête là
        if (noeudSuivant == null) {
            return null;
        }

        SimuSituation nouvelleSituation
                = new SimuSituation(noeudSuivant, joueurSuivant, stacksApresAction, joueurFolde, pot, potBounty);

        // on garde ça une map pour éviter de refaire les calculs
        situationsSuivantes.put(action, nouvelleSituation);

        return nouvelleSituation;
    }

    private float calculerPotBounty(HashMap<JoueurSimulation, Float> stacksDeparts,
                                    HashMap<JoueurSimulation, Float> stacksApresAction,
                                    HashMap<JoueurSimulation, Boolean> joueurFolde) {
        // todo : doublon avec initialisation de la première situation
        float potBounty = 0;
        for (JoueurSimulation joueur : stacksApresAction.keySet()) {
            // le bounty ne prend pas en compte les joueurs foldés
            if (joueurFolde.get(joueur)) {
                continue;
            }
            if (!(stacksDeparts.containsKey(joueur))) {
                throw new RuntimeException("Stack initial du joueur non trouvé");
            }
            float stackActuel = stacksApresAction.get(joueur);
            float stackInitial = stacksDeparts.get(joueur);
            potBounty += (stackInitial - stackActuel) * joueur.getBounty() / stackInitial;

        }

        return potBounty;
    }

    /**
     * détermine le joueur suivant sur la base des positions
     */
    private JoueurSimulation joueurSuivant(JoueurSimulation joueurPrecedent,
                                           HashMap<Integer, JoueurSimulation> joueurs,
                                           HashMap<JoueurSimulation, Float> stacksApresAction,
                                           HashMap<JoueurSimulation, Boolean> joueurFolde) {
        int positionInitiale = positionsJoueurs.get(joueurPrecedent);
        int positionCherchee = positionInitiale + 1;

        // on ne veut ni un joueur foldé ni un joueur dont le stack est à 0 (ce qui est facile à savoir)
        while(true) {
            if (positionCherchee == joueurs.size()) positionCherchee = 0;
            else if (positionCherchee == positionInitiale) break;

            JoueurSimulation joueurTeste = joueurs.get(positionCherchee);

            if (stacksApresAction.get(joueurTeste) > 0 && !(joueurFolde.get(joueurTeste))) {
                return joueurTeste;
            }

            positionCherchee++;
        }

        throw new RuntimeException("Aucun joueur trouvé");
    }

    private float calculerStackEffectif(JoueurSimulation joueur, HashMap<JoueurSimulation, Float> stacksActuels,
                                        HashMap<JoueurSimulation, Boolean> joueurFolde) {
        float maxStack = 0;
        for (JoueurSimulation joueurPresent : stacksActuels.keySet()) {
            if (joueurFolde.get(joueurPresent) || joueur == joueurPresent)
                continue;
            if (stacksActuels.get(joueurPresent) > maxStack) {
                maxStack = stacksActuels.get(joueurPresent);
            }
        }

        return (float) Math.min(maxStack, stacksActuels.get(joueur));
    }

    /**
     * on va ajouter les actions et les répertorier
     * @param situation
     */
    private void remplirSituation(SimuSituation situation) {
        NoeudSituation noeudSituation = situation.getNoeudSituation();
        for (NoeudAction noeudAction : noeudSituation.getNoeudsActions()) {
            NoeudAbstrait noeudAbstrait = new NoeudAbstrait(noeudAction.getIdNoeud());
            RangeSauvegardable rangeIso = noeudAction.getRange();
            // attention il faut multiplier betSize par taille du pot
            SimuAction simuAction =
                    new SimuAction(noeudAbstrait, rangeIso, noeudAction.getBetSize() * situation.getPot());
            situation.ajouterAction(simuAction);
        }
    }

    /**
     * on va initialiser les joueurs, avec un stack et un bounty "standard"
     */
    private void initialiserJoueurs(FormatSolution formatSolution) {
        joueurs.clear();
        positionsJoueurs.clear();
        for (int i = 0; i < formatSolution.getNombreJoueurs(); i++) {
            JoueurSimulation nouveauJoueur =
                    new JoueurSimulation(nomsPosition.get(i));
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
                nouveauJoueur.setBounty(0f);
            }
            joueurs.put(i, nouveauJoueur);
            positionsJoueurs.put(nouveauJoueur, i);
        }
    }
}
