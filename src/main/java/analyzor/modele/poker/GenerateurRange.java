package analyzor.modele.poker;

import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateurRange {
    private final List<ComboIso> combosTries = getCombosTries();

    public RangeReelle topRange(float pctCombos) {
        RangeReelle topRange = new RangeReelle();
        int nCombos = (int) (pctCombos * GenerateurCombos.combosReels.size());
        int combosAjoutes = 0;
        for (ComboIso comboIso : combosTries) {
            if (combosAjoutes >= nCombos) break;
            for (ComboReel comboReel : comboIso.toCombosReels()) {
                topRange.ajouterCombo(comboReel.toInt());
                combosAjoutes++;
            }
        }
        return topRange;
    }

    public RangeReelle bottomRange(float pctCombos) {
        RangeReelle topRange = new RangeReelle();
        int nCombos = (int) (pctCombos * GenerateurCombos.combosReels.size());
        int combosAjoutes = 0;
        for (int i = combosTries.size() - 1; i >= 0; i--) {
            ComboIso comboIso = combosTries.get(i);
            if (combosAjoutes >= nCombos) break;
            for (ComboReel comboReel : comboIso.toCombosReels()) {
                topRange.ajouterCombo(comboReel.toInt());
                combosAjoutes++;
            }
        }
        return topRange;
    }


    private List<ComboIso> getCombosTries() {
        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
        CalculatriceEquite calculatriceEquite = new CalculatriceEquite(configCalculatrice);

        List<ComboIso> tousLesCombos = GenerateurCombos.combosIso;

        List<RangeReelle> rangeVillains = new ArrayList<>();
        RangeReelle rangePleine = new RangeReelle();
        rangePleine.remplir();
        rangeVillains.add(rangePleine);

        Board boardVide = new Board();

        List<EquiteCombo> combosAvecEquite = new ArrayList<>();

        for (ComboIso combo : tousLesCombos) {

            ComboReel comboTeste = combo.toCombosReels().get(0);
            float equite = calculatriceEquite.equiteGlobaleMain(comboTeste, boardVide, rangeVillains);
            EquiteCombo nouveauCombo = new EquiteCombo(combo, equite);
            combosAvecEquite.add(nouveauCombo);
        }

        List<EquiteCombo> combosEquitesTries = combosAvecEquite.stream()
                .sorted(Comparator.comparingDouble(EquiteCombo::getEquite).reversed())
                .collect(Collectors.toList());

        List<ComboIso> combosTries = new ArrayList<>();
        for (EquiteCombo equiteCombo : combosEquitesTries) {
            combosTries.add(equiteCombo.combo);
        }

        return combosTries;

    }

    private static class EquiteCombo {
        protected ComboIso combo;
        protected float equite;

        protected EquiteCombo(ComboIso combo, float equite) {
            this.combo = combo;
            this.equite = equite;
        }

        public float getEquite() {return this.equite;}
    }
}
