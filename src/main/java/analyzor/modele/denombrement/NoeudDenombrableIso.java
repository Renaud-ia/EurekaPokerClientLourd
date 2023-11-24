package analyzor.modele.denombrement;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.denombrement.elements.ComboDenombrable;
import analyzor.modele.denombrement.elements.DenombrableIso;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.EquiteFuture;
import analyzor.modele.poker.evaluation.OppositionRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

// outil pour dénombrer les ranges préflop
public class NoeudDenombrableIso extends NoeudDenombrable {
    private final HashMap<ComboIso, ComboDenombrable> tableCombo;
    private static HashMap<ComboIso, EquiteFuture> equitesCalculees;
    public NoeudDenombrableIso(String nomNoeudAbstrait) {
        super(nomNoeudAbstrait);
        tableCombo = new HashMap<>();
    }

    @Override
    public void decompterCombos() {
        int indexAction = 0;
        for (NoeudAction noeudAction : this.getNoeudSansFold()) {
            denombrerCombos(noeudAction, indexAction);
            estimerShowdown(noeudAction, indexAction);
            indexAction++;
        }
    }

    private void denombrerCombos(NoeudAction noeudAction, int indexAction) {
        for (Entree entree : entreesCorrespondantes.get(noeudAction)) {
            ComboReel comboObserve = new ComboReel(entree.getCombo());
            ComboIso equivalentIso = new ComboIso(comboObserve);

            ComboDenombrable comboDenombrable = tableCombo.get(equivalentIso);
            if (comboDenombrable == null)
                throw new RuntimeException("Aucun équivalent dénombrable trouvé pour : " + comboObserve);

            comboDenombrable.incrementerObservation(indexAction);
        }
    }

    private void estimerShowdown(NoeudAction noeudAction, int indexAction) {
        float showdownAction = getShowdown(noeudAction);
        float moyenneEquite = 0;

        if (noeudAction.getMove() != Move.FOLD) {
            for (ComboDenombrable comboDenombrable : combosDenombrables) {
                moyenneEquite += comboDenombrable.getEquite();
            }
        }
        moyenneEquite /= combosDenombrables.size();

        for (ComboDenombrable comboDenombrable : combosDenombrables) {
            float valeurShowdown;
            // si all-in, le % showdown ne dépend pas du combo
            if (noeudAction.getMove() == Move.ALL_IN) {
                valeurShowdown = showdownAction;
            }
            else {
                valeurShowdown = (comboDenombrable.getEquite() / moyenneEquite) * showdownAction;
            }
            comboDenombrable.setShowdown(indexAction, valeurShowdown);
        }
    }

    /**
     * appelé par le classificateur
     */
    public void construireCombosPreflop(OppositionRange oppositionRange) {
        constructionTerminee();
        if (!(oppositionRange.getRangeHero() instanceof RangeIso))
            throw new IllegalArgumentException("La range fournie n'est pas une range iso");

        RangeIso rangeHero = (RangeIso) oppositionRange.getRangeHero();
        List<RangeReelle> rangesVillains = oppositionRange.getRangesVillains();

        if (equitesCalculees == null) calculerEquiteIso();

        for (ComboIso comboIso : rangeHero.getCombos()) {
            //todo est ce qu'on prend les combos nuls??
            if (comboIso.getValeur() == 0) continue;
            // on prend n'importe quel combo réel = même équité
            EquiteFuture equiteFuture = equitesCalculees.get(comboIso);

            DenombrableIso comboDenombrable = new DenombrableIso(
                    comboIso, comboIso.getValeur(), equiteFuture, this.getNombreActionsSansFold());
            this.combosDenombrables.add(comboDenombrable);
            this.tableCombo.put(comboIso, comboDenombrable);
        }
    }

    // todo : beaucoup trop long de calculer les équités au moment de ce code
    // en attendant on calcule les valeurs une seule fois
    // pas bon du tout pour % showdown probablement!!!!
    private void calculerEquiteIso() {
        logger.info("Hashmap non trouvé, on calcule les équites des combos iso");
        equitesCalculees = new HashMap<>();
        RangeReelle rangeVillain = new RangeReelle();
        rangeVillain.remplir();
        List<RangeReelle> rangesVillains = new ArrayList<>();
        rangesVillains.add(rangeVillain);

        Board board = new Board();

        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            ComboReel randomCombo = comboIso.toCombosReels().get(0);
            EquiteFuture equiteFuture = calculatriceEquite.equiteFutureMain(randomCombo, board, rangesVillains);
            equitesCalculees.put(comboIso, equiteFuture);
        }
    }

}
