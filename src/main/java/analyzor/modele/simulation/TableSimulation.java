package analyzor.modele.simulation;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;

import java.util.*;


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

    

    public void setSituationSelectionnee(SimuSituation situation) {
        if (!situations.contains(situation)) throw new IllegalArgumentException("SITUATION NON TROUVEE");
        situationActuelle = situation;
        joueurActuel = situation.getJoueur();

        
        actualiserRanges();
    }

    
    public Integer changerAction(SimuSituation situation, Integer indexAction) {
        return moteurJeu.fixerAction(situation, indexAction);
    }

    
    public Integer fixerActionParDefaut(SimuSituation situation) {
        
        Integer actionFixee = situation.actionFixee();
        if (actionFixee != null) {
            return actionFixee;
        }
        
        else return changerAction(situation, null);
    }

    

    
    public LinkedList<SimuSituation> situationsSuivantes(SimuSituation situation) {
        
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

        
        situations = new LinkedList<>(moteurJeu.getSuiteSituations());
        leafTrouvee = moteurJeu.leafTrouvee();

        
        return new LinkedList<>(situations.subList(indexSituation, situations.size()));
    }

    

    
    public LinkedList<TablePoker.JoueurTable> getJoueurs() {
        return moteurJeu.getJoueursSimulation();
    }

    public List<RangeReelle> getRangesVillains() {
        List<RangeReelle> rangesVillains = new ArrayList<>();
        for (TablePoker.JoueurTable joueurSimulation : moteurJeu.getJoueursSimulation()) {
            if (joueurSimulation == joueurActuel) continue;
            RangeIso rangeIso = rangesJoueurs.get(joueurSimulation);
            
            if (rangeIso != null) {
                rangesVillains.add(new RangeReelle(rangeIso));
            }
        }

        return rangesVillains;
    }

    
    public LinkedHashMap<SimuAction, RangeIso> getRangesSituationActuelle(Integer indexAction) {
        LinkedHashMap<SimuAction, RangeIso> ranges = new LinkedHashMap<>();
        
        RangeIso rangeTotale = rangesJoueurs.get(situationActuelle.getJoueur());
        if (rangeTotale == null) {
            throw new RuntimeException("Range non trouvé pour joueur actuel");
        }

        for (SimuAction action : situationActuelle.getActions()) {
            
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

    

    
    private void actualiserRanges() {
        
        for (TablePoker.JoueurTable joueurSimulation : moteurJeu.getJoueursSimulation()) {
            RangeIso rangeIso = new RangeIso();
            rangeIso.remplir();
            rangesJoueurs.put(joueurSimulation, rangeIso);
        }

        
        
        
        int indexSituation = situations.indexOf(situationActuelle);
        if (indexSituation == -1) throw new RuntimeException("Situation non trouvée");

        List<SimuSituation> situationsPrecedentes = situations.subList(0, indexSituation);
        for (SimuSituation situation : situationsPrecedentes) {
            TablePoker.JoueurTable joueurSituation = situation.getJoueur();
            RangeIso rangeJoueur = rangesJoueurs.get(joueurSituation);
            if (rangeJoueur == null) continue;

            SimuAction simuAction = situation.getActionActuelle();
            if (simuAction == null) throw new RuntimeException("Aucune action sélectionnée");

            
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
