package analyzor.modele.equilibrage;

import analyzor.modele.arbre.RecuperateurRange;
import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.equilibrage.elements.ComboDenombrable;
import analyzor.modele.parties.Entree;
import analyzor.modele.poker.RangeDenombrable;
import analyzor.modele.poker.RangeDynamique;
import analyzor.modele.poker.RangeIso;

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
    private RecuperateurRange recuperateurRange;
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

    public void ajouterRanges(RecuperateurRange recuperateurRange) {
        this.recuperateurRange = recuperateurRange;
    }

    public RangeDenombrable getRangeHero() {
        return recuperateurRange.getRangeHero();
    }

    /**
     * appelé lorsqu'on a fini de construire le noeud
     * garantit l'absence de modification
     * compte observations/showdown et construit les ComboDenombrable
     */
    public void constructionTerminee() {
        entreesCorrespondantes = Collections.unmodifiableMap(new LinkedHashMap<>(entreesCorrespondantes));
        denombrerObservationsShowdown();
        construireCombosDenombrables();
    }

    private void construireCombosDenombrables() {
        RangeDenombrable rangeDenombrable = this.getRangeHero();
        if (rangeDenombrable == null) throw new RuntimeException("Aucune range fournie");
        if (rangeDenombrable instanceof RangeIso) {
            construireCombosIso((RangeIso) rangeDenombrable);
        }
        else if (rangeDenombrable instanceof RangeDynamique) {
            construireCombosDynamiques((RangeDynamique) rangeDenombrable);
        }
        else throw new RuntimeException("La range n'est pas dénombrable");
    }

    private void construireCombosDynamiques(RangeDynamique rangeDenombrable) {
    }

    private void construireCombosIso(RangeIso rangeDenombrable) {

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
