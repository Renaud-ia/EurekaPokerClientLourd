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



class MoteurJeu extends TablePoker {
    
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
        
        if (indexAction == null) {
            indexAction = situation.fixerActionParDefaut();
        }

        
        else {
            situation.fixerAction(indexAction);
            construireSuiteSituations(situation);
        }

        return indexAction;
    }

    

    
    public LinkedList<SimuSituation> getSuiteSituations() {
        return situationsActuelles;
    }


    

    

    
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
        
        if (formatSolution.getAnteMax() > 0) {
            float valeurAnte = (formatSolution.getAnteMax() + formatSolution.getAnteMin()) * montantBB / 2 / 100;
            super.poserAntes(valeurAnte);
        }

        
        
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


    

    private void construireSuiteSituations(SimuSituation situation) {
        int indexSituation = situationsActuelles.indexOf(situation);
        if (indexSituation == -1) throw new IllegalArgumentException("Situation non trouvée");

        
        
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

    
    private void revenirSituation(SimuSituation situation) {
        potTable.reset();

        
        HashMap<JoueurTable, Float> stacks = situation.getStacks();
        
        HashMap<JoueurTable, Boolean> folde = situation.getJoueurFolde();
        for (JoueurTable joueurTable : getJoueurs()) {
            Float stack = stacks.get(joueurTable);
            if (stack == null)
                throw new IllegalArgumentException("Stack du joueur non stocké dans la situation : " + joueurTable);
            Boolean joueurFolde = folde.get(joueurTable);
            if (joueurFolde == null)
                throw new IllegalArgumentException("Fold du joueur non stocké dans la situation : " + joueurTable);

            float dejaInvesti = joueurTable.getStackInitial() - stack;

            
            if (formatSolution.getAnteMax() > 0) {
                float valeurAnte = (formatSolution.getAnteMax() + formatSolution.getAnteMin()) * montantBB / 2 / 100;
                dejaInvesti -= valeurAnte;
            }

            joueurTable.setStack(stack);
            joueurTable.setCouche(joueurFolde);
            joueurTable.setMontantInvesti(dejaInvesti);

            
            potTable.incrementer(dejaInvesti);
        }

        
        joueurActuel = situation.getJoueur();

        
        potTable.setDernierBet(situation.getDernierBet());

        

    }

    

    
    private SimuSituation creerSituation(SimuAction action) {
        
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
        
        float pot = potTable.potTotal() - potTable.getPotAnte();
        float potBounty = getPotBounty();

        SituationStackPotBounty situationStackPotBounty = new SituationStackPotBounty(
                stackEffectif,
                pot,
                potBounty
        );

        
        NoeudSituation noeudSuivant =
                recuperateurRange.noeudSituationPlusProche(
                        noeudAction, situationStackPotBounty, profilJoueur);

        
        if (noeudSuivant == null) {
            leafTrouvee = false;
            return null;
        }

        
        HashMap<JoueurTable, Float> stacksApresAction = new HashMap<>();
        HashMap<JoueurTable, Boolean> joueurFolde = new HashMap<>();

        for (JoueurTable joueurTable : getJoueurs()) {
            stacksApresAction.put(joueurTable, joueurTable.getStackActuel());
            joueurFolde.put(joueurTable, joueurTable.estCouche());
        }

        SimuSituation nouvelleSituation
                = new SimuSituation(noeudSuivant, joueurActuel, stacksApresAction,
                joueurFolde, pot, potBounty, potTable.getDernierBet());

        
        situationsDejaRecuperees.put(action, nouvelleSituation);

        return nouvelleSituation;
    }


    
    private JoueurTable joueurSuivant() {
        int positionInitiale = positionsJoueurs.get(joueurActuel);
        int positionCherchee = positionInitiale + 1;

        int maxCount = 0;
        
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

    
    private void remplirSituation(SimuSituation situation) {
        NoeudSituation noeudSituation = situation.getNoeudSituation();
        for (NoeudAction noeudAction : noeudSituation.getNoeudsActions()) {
            NoeudAbstrait noeudAbstrait = new NoeudAbstrait(noeudAction.getIdNoeud());
            RangeSauvegardable rangeIso = noeudAction.getRange();

            float betSize;
            

            
            if (noeudAbstrait.getMove() == Move.ALL_IN) {
                betSize = joueurActuel.getStackInitial();
            }

            
            else if (noeudAbstrait.getMove() == Move.CALL) {
                betSize = potTable.getDernierBet();
            }

            else if (noeudAbstrait.getMove() == Move.FOLD) {
                betSize = 0;
            }

            
            else {
                betSize = (noeudAction.getBetSize() * situation.getPot()) + joueurActuel.investiCeTour();
                
                betSize = Math.max(betSize, dernierBet() * 2);
            }
            
            SimuAction simuAction =
                    new SimuAction(noeudAbstrait, rangeIso, betSize);
            situation.ajouterAction(simuAction);
        }
    }

    public LinkedList<JoueurTable> getJoueursSimulation() {
        
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
