package analyzor.modele.arbre;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.equilibrage.elements.ComboDenombrable;
import analyzor.modele.equilibrage.elements.DenombrableIso;
import analyzor.modele.parties.Entree;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.poker.evaluation.OppositionRange;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * noeud construit par les différents classificateurs
 * peut ensuite être utilisé par showdown/dénombrement/equilibrage
 * todo : faire les méthodes pour obtenir les données
 */
public class NoeudDenombrable {
    // on veut garder l'ordre comme ça on ne stocke que des tableaux dans les ComboDenombrable
    private Map<NoeudAction, List<Entree>> entreesCorrespondantes;
    private int[] observationsGlobales;
    private float[] showdownsGlobaux;
    private float pShowdown;
    private List<ComboDenombrable> combosDenombrables;

    public NoeudDenombrable() {
        this.entreesCorrespondantes = new LinkedHashMap<>();
    }

    public void ajouterNoeud(NoeudAction noeudAction, List<Entree> entrees) {
        this.entreesCorrespondantes.put(noeudAction, entrees);
    }

    /**
     * appelé lorsqu'on a fini de construire le noeud
     * garantit l'absence de modification
     * compte observations/showdown et construit les ComboDenombrable
     */
    private void constructionTerminee() {
        entreesCorrespondantes = Collections.unmodifiableMap(new LinkedHashMap<>(entreesCorrespondantes));
        denombrerObservationsShowdown();
    }

    // utile pour construire denombrement et showdown adaptés
    public ComboDenombrable getComboDenombrable() {
        if (combosDenombrables == null || combosDenombrables.isEmpty())
            throw new RuntimeException("aucun combo dénombrable");
        return combosDenombrables.get(0);
    }

    public void construireCombosPreflop(OppositionRange oppositionRange) {
        constructionTerminee();


    }

    public void construireCombosSubset(OppositionRange oppositionRange, Board subset) {
        constructionTerminee();
    }

    public void construireCombosDynamique(RangeIso rangeIso) {
        constructionTerminee();
        for (ComboIso comboIso : rangeIso.getCombos()) {
            // on ne prend pas en compte les combos non présents dans range
            if (comboIso.getValeur() == 0) continue;

            ComboIso copieCombo = comboIso.copie();
            DenombrableIso comboDenombreable = new DenombrableIso(copieCombo);

        }
    }

    private void denombrerObservationsShowdown() {
        int index = 0;
        int totalEntrees = 0;

        observationsGlobales = new int[entreesCorrespondantes.size()];
        showdownsGlobaux = new float[entreesCorrespondantes.size()];

        for (List<Entree> entrees : entreesCorrespondantes.values()) {
            observationsGlobales[index] = entrees.size();

            int nShowdown = 0;
            for (Entree entree : entrees) {
                if (entree.getCartesJoueur() != 0) nShowdown++;
            }
            float pShowdownAction = (float) nShowdown / entrees.size();

            showdownsGlobaux[index] = pShowdownAction;
            this.pShowdown += entrees.size() * pShowdownAction;

            index++;
            totalEntrees += entrees.size();
        }
        this.pShowdown /= totalEntrees;
    }
}
