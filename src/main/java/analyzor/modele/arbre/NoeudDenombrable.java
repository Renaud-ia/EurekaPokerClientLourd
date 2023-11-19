package analyzor.modele.arbre;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.equilibrage.elements.ComboDenombrable;
import analyzor.modele.equilibrage.elements.DenombrableIso;
import analyzor.modele.parties.Entree;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.modele.poker.evaluation.EquiteFuture;
import analyzor.modele.poker.evaluation.OppositionRange;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * noeud construit par les différents classificateurs
 * construit tout seul les combos dénombrables une fois qu'on lui rentre une range
 * clusterise les combos dynamiques
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
    private final CalculatriceEquite calculatriceEquite;

    public NoeudDenombrable() {
        this.entreesCorrespondantes = new LinkedHashMap<>();
        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);
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
    public List<ComboDenombrable> getCombosDenombrables() {
        if (combosDenombrables == null || combosDenombrables.isEmpty())
            throw new RuntimeException("aucun combo dénombrable");
        return combosDenombrables;
    }

    public void construireCombosPreflop(OppositionRange oppositionRange) {
        constructionTerminee();
        if (!(oppositionRange.getRangeHero() instanceof RangeIso))
            throw new IllegalArgumentException("La range fournie n'est pas une range iso");

        RangeIso rangeHero = (RangeIso) oppositionRange.getRangeHero();
        List<RangeReelle> rangesVillains = oppositionRange.getRangesVillains();

        for (ComboIso comboIso : rangeHero.getCombos()) {
            //todo est ce qu'on prend les combos nuls??
            if (comboIso.getValeur() == 0) continue;
            // on prend n'importe quel combo réel = même équité
            ComboReel randomCombo = comboIso.toCombosReels().get(0);
            Board board = new Board();
            EquiteFuture equiteFuture = calculatriceEquite.equiteFutureMain(randomCombo, board, rangesVillains);
            float equite = calculatriceEquite.equiteGlobaleMain(randomCombo, board, rangesVillains);

            DenombrableIso comboDenombrable = new DenombrableIso(comboIso, comboIso.getValeur(), equiteFuture, equite);
            this.combosDenombrables.add(comboDenombrable);
        }
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

    public NoeudAction[] getNoeudsActions() {
        return entreesCorrespondantes.keySet().toArray(new NoeudAction[0]);
    }
}
