package analyzor.modele.denombrement;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.modele.poker.evaluation.OppositionRange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * noeud construit par les différents classificateurs
 * construit tout seul les combos dénombrables une fois qu'on lui rentre une range
 * clusterise les combos dynamiques
 * remplit les showdown/denombrement
 */
public abstract class NoeudDenombrable {
    // on veut garder l'ordre comme ça on ne stocke que des tableaux dans les ComboDenombrable
    protected final Logger logger = LogManager.getLogger(NoeudDenombrable.class);
    protected Map<NoeudAction, List<Entree>> entreesCorrespondantes;
    private HashMap<NoeudAction, Integer> observationsGlobales;
    private HashMap<NoeudAction, Float> showdownsGlobaux;
    private float pShowdown;
    protected CalculatriceEquite calculatriceEquite;
    private final String nomNoeudAbstrait;
    // important pour not_folded => on veut des combos triés par équité décroissante
    protected LinkedList<ComboDenombrable> combosDenombrables;
    protected List<NoeudAction> noeudsSansFold;

    public NoeudDenombrable(String nomNoeudAbstrait) {
        this.entreesCorrespondantes = new LinkedHashMap<>();
        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);
        this.nomNoeudAbstrait = nomNoeudAbstrait;
        this.combosDenombrables = new LinkedList<>();
        observationsGlobales = new HashMap<>();
        showdownsGlobaux = new HashMap<>();
    }

    // décompte les observations et showdowns
    public abstract void decompterCombos();

    public List<ComboDenombrable> getCombosDenombrables() {
        return this.combosDenombrables;
    }

    public void ajouterNoeud(NoeudAction noeudAction, List<Entree> entrees) {
        this.entreesCorrespondantes.put(noeudAction, entrees);
    }

    /**
     * appelé lorsqu'on a fini de construire le noeud
     * garantit l'absence de modification
     * compte observations/showdown et construit les ComboDenombrable
     */
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

    /**
     * on calcule les observations globales
     */
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

    // retourne avec fold
    public NoeudAction[] getNoeudsActions() {
        return entreesCorrespondantes.keySet().toArray(new NoeudAction[0]);
    }

    public List<Entree> obtenirEchantillon() {
        // on récupère juste une entrée par action
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
        int stackEffectif = (int) entreesCorrespondantes.keySet().iterator().next().getStackEffectif();
        return stackEffectif + "bb" + nomNoeudAbstrait;
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
        // important il faut conserver l'ordre
        float[] pActions = new float[noeudsSansFold.size() + 1];
        for (int i = 0; i < noeudsSansFold.size(); i++) {
            pActions[i] = (float) entreesCorrespondantes.get(noeudsSansFold.get(i)).size() / totalEntrees();
        }
        pActions[pActions.length - 1] = getPFold();
        return pActions;
    }

    private float getPFold() {
        for (NoeudAction noeudAction : entreesCorrespondantes.keySet()) {
            if (noeudAction.getMove() == Move.FOLD)
                return (float) entreesCorrespondantes.get(noeudAction).size() / totalEntrees();
        }
        throw new RuntimeException("FOLD NON TROUVE DANS LES ACTIONS");
    }
}
