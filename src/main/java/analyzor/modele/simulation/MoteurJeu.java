package analyzor.modele.simulation;

import analyzor.modele.arbre.RecuperateurRange;
import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.arbre.noeuds.NoeudSituation;
import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import analyzor.modele.poker.RangeSauvegardable;

import java.util.HashMap;
import java.util.LinkedList;


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
        super(1);
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
        initialiserJoueurs(formatSolution, false);
        resetSituations();
    }

    boolean resetSituations() {
        super.reset();
        return initialiserSituations();
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
    public void initialiserJoueurs(FormatSolution formatSolution, boolean modeHU) {
        mapJoueursNom.clear();
        mapJoueursPositions.clear();
        positionsJoueurs.clear();

        this.nouveauTour();

        int nJoueurs;
        if (modeHU) {
            nJoueurs = 2;
        }
        else {
            nJoueurs = formatSolution.getNombreJoueurs();
        }

        for (int i = 0; i < nJoueurs; i++) {
            // todo : est ce que on fait un autre stack pour CASH GAME ?
            float stackDepart;
            if (formatSolution.getPokerFormat() == Variante.PokerFormat.SPIN) {
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

            super.ajouterJoueur(nomsPosition.get(i), nouveauJoueur);
        }
    }

    private boolean initialiserSituations() {
        situationsDejaRecuperees.clear();
        situationsActuelles.clear();
        SimuSituation premiereSituation = premiereSituation();
        if (premiereSituation == null) {
            return false;
        }
        remplirSituation(premiereSituation);
        premiereSituation.fixerActionParDefaut();

        situationsActuelles.add(premiereSituation);
        construireSuiteSituations(premiereSituation);

        return true;
    }

    private SimuSituation premiereSituation() {
        int nJoueurs = nombreJoueursActifs();
        // on ajoute les ante pour chaque joueur si existe
        if (formatSolution.getAnteMax() > 0) {
            float valeurAnte = (formatSolution.getAnteMax() + formatSolution.getAnteMin()) * montantBB / 2 / 100;
            super.poserAntes(valeurAnte);
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
            leafTrouvee = true;
            return;
        }
        // tant qu'on arrive à créer un noeud, on fixe une action par défaut et on construit la situation suivante
        while ((situation = creerSituation(action)) != null) {
            remplirSituation(situation);
            situationsActuelles.add(situation);
            situation.deselectionnerAction();
            situation.fixerActionParDefaut();
            action = situation.getActionActuelle();
            if (action.isLeaf()) {
                leafTrouvee = true;
                break;
            }
        }
    }

    /**
     * méthode qui permet de remettre la table dans l'état de la situation
     * chose qu'on ne peut pas connaitre dans version actuelle : le stage, le nombre d'actions
     * @param situation la situation à laquelle on veut revenir
     */
    private void revenirSituation(SimuSituation situation) {
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

            // important il faut retirer l'ante du joueur sinon le montant du pot doit être sans ante
            if (formatSolution.getAnteMax() > 0) {
                float valeurAnte = (formatSolution.getAnteMax() + formatSolution.getAnteMin()) * montantBB / 2 / 100;
                dejaInvesti -= valeurAnte;
            }

            joueurTable.setStack(stack);
            joueurTable.setCouche(joueurFolde);
            joueurTable.setMontantInvesti(dejaInvesti);

            // on remet le pot
            potTable.incrementer(dejaInvesti);
        }

        // on remet le joueur actuel
        joueurActuel = situation.getJoueur();

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
        // on vérifie qu'on l'a pas déjà récupérée
        SimuSituation situationDejaRecuperee = situationsDejaRecuperees.get(action);
        if (situationDejaRecuperee != null) {
            return situationDejaRecuperee;
        }

        JoueurTable joueurAction = joueurActuel;

        Long noeudAction;
        if (action == null) {
            noeudAction = new NoeudAbstrait(mapJoueursPositions.size(), TourMain.Round.PREFLOP).toLong();
        }
        else {
            noeudAction = action.getIdNoeud();
            super.ajouterAction(joueurAction, action.getMove(),
                    action.getBetSize(), true);
        }

        joueurActuel = joueurSuivant();

        if (joueurActuel == null) {
            return null;
        }
        ProfilJoueur profilJoueur;
        if (joueurActuel.estHero()) {
            profilJoueur = ObjetUnique.selectionnerHero();
        }
        else {
            profilJoueur = ObjetUnique.selectionnerVillain();
        }

        StacksEffectifs stackEffectif = stackEffectif();
        // on ne prend pas en compte les ante
        float pot = potTable.potTotal() - potTable.getPotAnte();
        float potBounty = getPotBounty();

        SituationStackPotBounty situationStackPotBounty = new SituationStackPotBounty(
                stackEffectif,
                pot,
                potBounty
        );

        // on vérifie qu'on trouve une situation correspondante dans la BDD
        NoeudSituation noeudSuivant =
                recuperateurRange.noeudSituationPlusProche(
                        noeudAction, situationStackPotBounty, profilJoueur);

        // on a rien trouvé dans la base on s'arrête
        if (noeudSuivant == null) {
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

        SimuSituation nouvelleSituation
                = new SimuSituation(noeudSuivant, joueurActuel, stacksApresAction,
                joueurFolde, pot, potBounty, potTable.getDernierBet());

        // on garde ça une map pour éviter de refaire les calculs
        situationsDejaRecuperees.put(action, nouvelleSituation);

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
                return null;
            }

            JoueurTable joueurTeste = mapJoueursPositions.get(positionCherchee);
            if (joueurTeste == null) {
                positionCherchee = 0;
                continue;
            }

            if (joueurTeste.getStackActuel() > 0 && (!(joueurTeste.estCouche()))) {
                return joueurTeste;
            }

            positionCherchee++;

            if (maxCount++ > 12) throw new RuntimeException("Aucun joueur trouvé");
        }
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

            float betSize;
            // on veut des bet complets

            // all-in c'est le stack initial
            if (noeudAbstrait.getMove() == Move.ALL_IN) {
                betSize = joueurActuel.getStackInitial();
            }

            // call c'est le montant du dernier bet
            else if (noeudAbstrait.getMove() == Move.CALL) {
                betSize = potTable.getDernierBet();
            }

            else if (noeudAbstrait.getMove() == Move.FOLD) {
                betSize = 0;
            }

            // autrement c'est le montant du bet size (=bet supplementaire) + montant déjà investi => bet total
            else {
                betSize = (noeudAction.getBetSize() * situation.getPot()) + joueurActuel.investiCeTour();
                // pas moins de *2 dernier bet
                betSize = Math.max(betSize, dernierBet() * 2);
            }
            // attention il faut multiplier betSize par taille du pot
            SimuAction simuAction =
                    new SimuAction(noeudAbstrait, rangeIso, betSize);
            situation.ajouterAction(simuAction);
        }
    }

    public LinkedList<JoueurTable> getJoueursSimulation() {
        // todo ici gérer l'ordre des joueurs pour affichage
        LinkedList<JoueurTable> joueursDansLOrdre = new LinkedList<>();
        for (int i : mapJoueursPositions.keySet()) {
            joueursDansLOrdre.add(mapJoueursPositions.get(i));
        }
        return joueursDansLOrdre;
    }

    public boolean leafTrouvee() {
        return leafTrouvee;
    }

    public boolean estInitialisee() {
        return formatSolution != null;
    }
}
