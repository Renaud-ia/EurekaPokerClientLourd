package analyzor.modele.denombrement;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.modele.poker.evaluation.OppositionRange;
import analyzor.modele.simulation.BuilderStackEffectif;
import analyzor.modele.simulation.StacksEffectifs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public abstract class NoeudDenombrable {
    protected Map<NoeudAction, List<Entree>> entreesCorrespondantes;
    private HashMap<NoeudAction, Integer> observationsGlobales;
    private HashMap<NoeudAction, Float> showdownsGlobaux;
    private float pShowdown;
    private final String nomNoeudAbstrait;

    protected LinkedList<ComboDenombrable> combosDenombrables;
    protected List<NoeudAction> noeudsSansFold;

    public NoeudDenombrable(String nomNoeudAbstrait) {
        this.entreesCorrespondantes = new LinkedHashMap<>();
        this.nomNoeudAbstrait = nomNoeudAbstrait;
        this.combosDenombrables = new LinkedList<>();
        observationsGlobales = new HashMap<>();
        showdownsGlobaux = new HashMap<>();
    }


    public abstract void decompterCombos();

    public List<ComboDenombrable> getCombosDenombrables() {
        return this.combosDenombrables;
    }

    public void ajouterNoeud(NoeudAction noeudAction, List<Entree> entrees) {
        this.entreesCorrespondantes.put(noeudAction, entrees);
    }

    
    public void constructionTerminee() {
        entreesCorrespondantes = Collections.unmodifiableMap(new LinkedHashMap<>(entreesCorrespondantes));
        noeudsSansFold = new ArrayList<>();
        for (NoeudAction noeudAction : entreesCorrespondantes.keySet()) {
            if (noeudAction.getMove() != Move.FOLD) {
                noeudsSansFold.add(noeudAction);
            }
        }
        noeudsSansFold = Collections.unmodifiableList(noeudsSansFold);
        denombrerObservationsShowdown();
    }

    public void construireCombosSubset(OppositionRange oppositionRange, Board subset) {
        constructionTerminee();
    }

    public void construireCombosDynamique(RangeIso rangeIso) {
        constructionTerminee();
    }

    
    private void denombrerObservationsShowdown() {
        int totalEntrees = 0;
        for (NoeudAction noeudAction : getNoeudsActions()) {
            List<Entree> entrees = entreesCorrespondantes.get(noeudAction);

            int nShowdown = 0;
            for (Entree entree : entrees) {
                if (entree.getCartesJoueur() != 0) nShowdown++;
            }
            float pShowdownAction = (float) nShowdown / entrees.size();

            observationsGlobales.put(noeudAction, entrees.size());
            showdownsGlobaux.put(noeudAction, pShowdownAction);
            this.pShowdown += entrees.size() * pShowdownAction;

            totalEntrees += entrees.size();
        }
        this.pShowdown /= totalEntrees;
    }


    public NoeudAction[] getNoeudsActions() {
        return entreesCorrespondantes.keySet().toArray(new NoeudAction[0]);
    }

    public List<Entree> obtenirEchantillon() {

        List<Entree> echantillon = new ArrayList<>();
        for (List<Entree> entreesAction : entreesCorrespondantes.values()) {
            Random random = new Random();
            int randomIndex = random.nextInt(entreesAction.size());
            echantillon.add(entreesAction.get(randomIndex));
        }
        return echantillon;
    }

    public int getObservation(NoeudAction noeudAction) {
        return observationsGlobales.get(noeudAction);
    }

    public float getShowdown(NoeudAction noeudAction) {
        return showdownsGlobaux.get(noeudAction);
    }

    @Override
    public String toString() {
        long codeStackEffectif = entreesCorrespondantes.keySet().iterator().next().getCodeStackEffectif();
        StacksEffectifs stacksEffectifs = BuilderStackEffectif.getStacksEffectifs(codeStackEffectif);
        return stacksEffectifs + "bb" + nomNoeudAbstrait;
    }

    public int getNombreActions() {
        return entreesCorrespondantes.size();
    }

    public List<Entree> getEntrees(NoeudAction noeudAction) {
        return entreesCorrespondantes.get(noeudAction);
    }

    public int totalEntrees() {
        int totalEntrees = 0;
        for (int observation : observationsGlobales.values()) {
            totalEntrees += observation;
        }

        return totalEntrees;
    }

    protected int getNombreActionsSansFold() {
        int actionsSansFold = 0;
        for (NoeudAction noeudAction : entreesCorrespondantes.keySet()) {
            if (noeudAction.getMove() != Move.FOLD) {
                actionsSansFold++;
            }
        }
        return actionsSansFold;
    }

    public List<NoeudAction> getNoeudSansFold() {
        return noeudsSansFold;
    }

    public float[] getPActions() {

        float[] pActions;
        if (getPFold() == null) {
            pActions = new float[noeudsSansFold.size()];
        }
        else {
            pActions = new float[noeudsSansFold.size() + 1];
            pActions[pActions.length - 1] = getPFold();
        }

        for (int i = 0; i < noeudsSansFold.size(); i++) {
            pActions[i] = (float) entreesCorrespondantes.get(noeudsSansFold.get(i)).size() / totalEntrees();
        }

        return pActions;
    }

    public Float getPFold() {
        for (NoeudAction noeudAction : entreesCorrespondantes.keySet()) {
            if (noeudAction.getMove() == Move.FOLD)
                return (float) entreesCorrespondantes.get(noeudAction).size() / totalEntrees();
        }
        return null;
    }

    public NoeudAction getNoeudFold() {
        for (NoeudAction noeudAction : entreesCorrespondantes.keySet()) {
            if (noeudAction.getMove() == Move.FOLD)
                return noeudAction;
        }
        return null;
    }



    public abstract void decompterStrategieReelle();

    protected List<Entree> toutesLesEntrees() {
        List<Entree> toutesLesEntrees = new ArrayList<>();
        for (List<Entree> entreesAction : entreesCorrespondantes.values()) {
            toutesLesEntrees.addAll(entreesAction);
        }

        return toutesLesEntrees;
    }
}
