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
    private HashMap<ComboIso, ComboDenombrable> tableCombo;
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

        for (ComboIso comboIso : rangeHero.getCombos()) {
            System.out.println("COMBO CONSTRUIT : " + comboIso);
            //todo est ce qu'on prend les combos nuls??
            if (comboIso.getValeur() == 0) continue;
            // on prend n'importe quel combo réel = même équité
            ComboReel randomCombo = comboIso.toCombosReels().get(0);
            Board board = new Board();
            EquiteFuture equiteFuture = calculatriceEquite.equiteFutureMain(randomCombo, board, rangesVillains);

            DenombrableIso comboDenombrable = new DenombrableIso(
                    comboIso, comboIso.getValeur(), equiteFuture, this.getNombreActionsSansFold());
            this.combosDenombrables.add(comboDenombrable);
            this.tableCombo.put(comboIso, comboDenombrable);
        }
    }

}
