package analyzor.modele.simulation;

import analyzor.modele.arbre.RecuperateurRange;
import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.arbre.noeuds.NoeudSituation;
import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.config.ValeursConfig;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import analyzor.modele.poker.RangeSauvegardable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


/**
 * surcouche de TablePoker qui initialise la table, permet de naviguer et de trouver le prochain joueur actif
 * en interaction avec l'abre abstrait et récupérateur Range, vérifie que les situations et les actions existent
 * crée les situations
 * fixe le nom des positions
 * garde les traces des stacks
 * est responsable de la hiérarchie des actions
 * objet SimuSituation stocke toutes les infos pour remettre la table dans l'état où elle était
 * todo : comment on va faire postflop ?
 */
class MoteurJeu extends TablePoker {
    // on stocke les informations immuables, le reste est dans les situations
    private HashMap<Integer, String> nomsPosition;
    private final HashMap<Integer, JoueurTable> mapJoueursPositions;
    private final HashMap<JoueurTable, Integer> positionsJoueurs;
    private final LinkedList<SimuSituation> situationsActuelles;
    private final HashMap<SimuAction, SimuSituation> situationsDejaRecuperees;
    private RecuperateurRange recuperateurRange;
    private FormatSolution formatSolution;
    private boolean leafTrouvee;

    MoteurJeu() {
        super(1, false);
        mapJoueursPositions = new HashMap<>();
        positionsJoueurs = new HashMap<>();
        situationsActuelles = new LinkedList<>();
        situationsDejaRecuperees = new HashMap<>();
    }

    // interface de controle par TablePoker

    void reset(FormatSolution formatSolution) {
        super.reset();
        this.formatSolution = formatSolution;
        recuperateurRange = new RecuperateurRange(formatSolution);
        nomsPosition = NomsPositions.obtNoms(formatSolution.getNombreJoueurs());
        initialiserJoueurs(formatSolution);
        resetSituations();
    }

    void resetSituations() {
        super.reset();
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

    /**
     * renvoie toutes les situations actuelles
     */
    public LinkedList<SimuSituation> getSuiteSituations() {
        return situationsActuelles;
    }


    // méthodes privées

    // initialisation de la table

    /**
     * on va initialiser les mapJoueursPositions, avec un stack et un bounty "standard"
     * SEAT BTN = 0
     */
    private void initialiserJoueurs(FormatSolution formatSolution) {
        mapJoueursNom.clear();
        mapJoueursPositions.clear();
        positionsJoueurs.clear();

        for (int i = 0; i < formatSolution.getNombreJoueurs(); i++) {
            // todo : est ce que on fait un autre stack pour CASH GAME ?
            float stackDepart;
            if (formatSolution.getNomFormat() == Variante.PokerFormat.SPIN) {
                stackDepart = 25;
            }
            else {
                stackDepart = 100;
            }

            float bounty;
            if (formatSolution.getKO()) {
                bounty = (float) formatSolution.getMinBuyIn() / 2;
            }
            else {
                bounty = 0f;
            }

            JoueurTable nouveauJoueur = new JoueurTable(nomsPosition.get(i), stackDepart, bounty);

            mapJoueursPositions.put(i, nouveauJoueur);
            positionsJoueurs.put(nouveauJoueur, i);

            mapJoueursNom.put(nomsPosition.get(i), nouveauJoueur);

            logger.trace("JOUEUR CREE : " + nouveauJoueur);
        }
    }

    private void initialiserSituations() {
        situationsDejaRecuperees.clear();
        situationsActuelles.clear();
        SimuSituation premiereSituation = premiereSituation();
        remplirSituation(premiereSituation);
        premiereSituation.fixerActionParDefaut();

        situationsActuelles.add(premiereSituation);
        construireSuiteSituations(premiereSituation);
    }

    private SimuSituation premiereSituation() {
        System.out.println("PREMIERE SITUATION");
        // todo: pour débug, à supprimer
        for (JoueurTable joueurTable : getJoueurs()) {
            System.out.println("STACK (" + joueurTable.getNom() + ") : " + joueurTable.getStackActuel());
        }

        int nJoueurs = this.nouveauTour();
        // on ajoute les ante pour chaque joueur si existe
        if (formatSolution.getAnte()) {
            float valeurAnte = 0.15f;

            for (int i = 0; i < mapJoueursPositions.size(); i++) {
                JoueurTable joueurTraite = mapJoueursPositions.get(i);
                if (joueurTraite == null) throw new RuntimeException("Joueur non trouvé pour index : " + i);

                logger.trace("Ajout d'ante pour : " + joueurTraite);
                this.ajouterAnte(joueurTraite, valeurAnte);
            }
        }

        // on ajoute les blindes
        // on fixe le joueur en grosse blinde, creerSituation va sélectionner le joueur qui suit
        if (nJoueurs == 2) {
            super.ajouterBlindes(mapJoueursPositions.get(1), mapJoueursPositions.get(0));
            joueurActuel = mapJoueursPositions.get(1);
        }
        else {
            super.ajouterBlindes(mapJoueursPositions.get(2), mapJoueursPositions.get(1));
            joueurActuel = mapJoueursPositions.get(2);
        }

        return creerSituation(null);
    }


    // navigation dans l'arbre

    private void construireSuiteSituations(SimuSituation situation) {
        int indexSituation = situationsActuelles.indexOf(situation);
        if (indexSituation == -1) throw new IllegalArgumentException("Situation non trouvée");

        logger.trace("Reconstruction des situations à partir de l'index : " + indexSituation);

        // on fixe les actions par défaut des situations + 1
        // puis on les supprime
        for (int i = situationsActuelles.size() - 1; i > indexSituation; i--) {
            SimuSituation simuSituation = situationsActuelles.get(i);
            simuSituation.deselectionnerAction();
            simuSituation.fixerActionParDefaut();
            situationsActuelles.remove(i);
        }

        revenirSituation(situation);

        SimuAction action = situation.getActionActuelle();
        if (action.isLeaf()) {
            // todo pour plus tard on voudrait savoir si leaf avec flop ou non
            logger.trace("L'action cliquée est une leaf");
            leafTrouvee = true;
            return;
        }
        logger.trace("ACTION ACTUELLE : " + action);
        // tant qu'on arrive à créer un noeud, on fixe une action par défaut et on construit la situation suivante
        while ((situation = creerSituation(action)) != null) {
            logger.trace("SITUATION CREE");
            remplirSituation(situation);
            situationsActuelles.add(situation);
            situation.deselectionnerAction();
            situation.fixerActionParDefaut();
            action = situation.getActionActuelle();
            if (action.isLeaf()) {
                logger.trace("On a trouvé une leaf");
                leafTrouvee = true;
                break;
            }
        }

        logger.trace(situationsActuelles);
    }

    /**
     * méthode qui permet de remettre la table dans l'état de la situation
     * chose qu'on ne peut pas connaitre dans version actuelle : le stage, le nombre d'actions
     * @param situation la situation à laquelle on veut revenir
     */
    private void revenirSituation(SimuSituation situation) {
        logger.trace("Réinitialisation de la table à situation antérieure");
        potTable.reset();

        // on remet les stacks des joueurs et on fixe ce qu'ils ont déjà investi
        HashMap<JoueurTable, Float> stacks = situation.getStacks();
        // on récupère les joueurs qui ont foldé
        HashMap<JoueurTable, Boolean> folde = situation.getJoueurFolde();
        for (JoueurTable joueurTable : getJoueurs()) {
            Float stack = stacks.get(joueurTable);
            if (stack == null)
                throw new IllegalArgumentException("Stack du joueur non stocké dans la situation : " + joueurTable);
            Boolean joueurFolde = folde.get(joueurTable);
            if (joueurFolde == null)
                throw new IllegalArgumentException("Fold du joueur non stocké dans la situation : " + joueurTable);

            float dejaInvesti = joueurTable.getStackInitial() - stack;

            joueurTable.setStack(stack);
            joueurTable.setCouche(joueurFolde);
            joueurTable.setMontantInvesti(dejaInvesti);

            // on remet le pot
            potTable.incrementer(dejaInvesti);
        }

        // on remet le joueur actuel
        joueurActuel = situation.getJoueur();
        logger.trace("Stack du joueur de la situation : " + joueurActuel.getStackActuel());

        // on remet le dernier bet
        potTable.setDernierBet(situation.getDernierBet());

        // le pot bounty ne change pas

    }

    // génération des situations

    /**
     * crée un noeud situation à partir d'un id de Noeud
     * va vérifier qu'il existe dans la base
     * et enregistrer les bonnes valeurs (mapJoueursPositions, stacks, etc.)
     * la table doit être à jour
     * @param action est le noeud de l'action à partir de laquelle on veut générér une situation
     * @return la simuSituation, null si on a pas trouvé dans la BDD
     */
    private SimuSituation creerSituation(SimuAction action) {
        logger.trace("Création de la situation correspondante");
        // on vérifie qu'on l'a pas déjà récupérée
        SimuSituation situationDejaRecuperee = situationsDejaRecuperees.get(action);
        if (situationDejaRecuperee != null) {
            return situationDejaRecuperee;
        }

        JoueurTable joueurAction = joueurActuel;

        Long noeudAction;
        if (action == null) {
            // todo ici il faut créer la première situation
            noeudAction = new NoeudAbstrait(mapJoueursPositions.size(), TourMain.Round.PREFLOP).toLong();
        }
        else {
            noeudAction = action.getIdNoeud();
            // todo vérifier si c'est vraiment betTotal
            super.ajouterAction(joueurAction, action.getMove(), action.getBetSize(), true);
        }

        float stackEffectif = stackEffectif();
        float pot = potTable.potTotal();
        float potBounty = getPotBounty();

        JoueurTable joueurSuivant = joueurSuivant();
        if (joueurSuivant == null) {
            return null;
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
                        noeudAction, stackEffectif, pot, potBounty, profilJoueur);

        // on a rien trouvé dans la base on s'arrête là
        if (noeudSuivant == null) {
            logger.trace("AUCUNE SUITE TROUVEE");
            leafTrouvee = false;
            return null;
        }

        // on récupère les infos de la table et on les mets dans la situation pour pouvoir les récupérer
        HashMap<JoueurTable, Float> stacksApresAction = new HashMap<>();
        HashMap<JoueurTable, Boolean> joueurFolde = new HashMap<>();

        for (JoueurTable joueurTable : getJoueurs()) {
            stacksApresAction.put(joueurTable, joueurTable.getStackActuel());
            joueurFolde.put(joueurTable, joueurTable.estCouche());
        }

        logger.trace(stacksApresAction);

        SimuSituation nouvelleSituation
                = new SimuSituation(noeudSuivant, joueurSuivant, stacksApresAction,
                joueurFolde, pot, potBounty, potTable.getDernierBet());

        // on garde ça une map pour éviter de refaire les calculs
        situationsDejaRecuperees.put(action, nouvelleSituation);

        joueurActuel = joueurSuivant;

        return nouvelleSituation;
    }


    /**
     * détermine le joueur suivant sur la base des positions
     */
    private JoueurTable joueurSuivant() {
        int positionInitiale = positionsJoueurs.get(joueurActuel);
        int positionCherchee = positionInitiale + 1;

        int maxCount = 0;
        // on ne veut ni un joueur foldé ni un joueur dont le stack est à 0 (ce qui est facile à savoir)
        while(true) {
            if (positionCherchee == positionInitiale) {
                logger.error("Aucun joueur trouvé");
                return null;
            }

            logger.trace("POSITION CHERCHE : " + positionCherchee);

            JoueurTable joueurTeste = mapJoueursPositions.get(positionCherchee);
            if (joueurTeste == null) {
                positionCherchee = 0;
                continue;
            }

            logger.trace(joueurTeste.getStackActuel());
            logger.trace(joueurTeste.estCouche());

            if (joueurTeste.getStackActuel() > 0 && (!(joueurTeste.estCouche()))) {
                return joueurTeste;
            }

            positionCherchee++;

            if (maxCount++ > ValeursConfig.MAX_JOUEURS) throw new RuntimeException("Aucun joueur trouvé");
        }
    }

    /**
     * on va ajouter les actions et les répertorier
     * @param situation
     */
    private void remplirSituation(SimuSituation situation) {
        logger.trace("Remplissage de la situation");
        NoeudSituation noeudSituation = situation.getNoeudSituation();
        for (NoeudAction noeudAction : noeudSituation.getNoeudsActions()) {
            NoeudAbstrait noeudAbstrait = new NoeudAbstrait(noeudAction.getIdNoeud());
            RangeSauvegardable rangeIso = noeudAction.getRange();

            float betSize;
            logger.trace("Action trouvee : " + noeudAbstrait);
            // en cas de all-in le joueur met le stack qui lui reste
            if (noeudAbstrait.getMove() == Move.ALL_IN) {
                logger.trace("Le joueur est all-in");
                betSize = situation.getStacks().get(situation.getJoueur());
            }
            else if (noeudAbstrait.getMove() == Move.CALL) {
                betSize = potTable.getDernierBet() - joueurActuel.montantInvesti();
            }
            else {
                betSize = noeudAction.getBetSize() * situation.getPot();
            }
            // attention il faut multiplier betSize par taille du pot
            SimuAction simuAction =
                    new SimuAction(noeudAbstrait, rangeIso, betSize);
            situation.ajouterAction(simuAction);
        }
    }

    public Set<JoueurTable> getJoueursSimulation() {
        return new HashSet<>(mapJoueursPositions.values());
    }

    public boolean leafTrouvee() {
        return leafTrouvee;
    }

    public boolean estInitialisee() {
        return formatSolution != null;
    }
}
