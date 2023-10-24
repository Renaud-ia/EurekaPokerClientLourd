package analyzor.modele.poker;

import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateurRange {
    private final int[] combosTries = getCombosTries();

    public RangeReelle topRange(float pctCombos) {
        RangeReelle topRange = new RangeReelle();
        int nCombos = (int) (pctCombos * GenerateurCombos.combosReels.size());
        for (int i = 0; i < nCombos; i++) {
            topRange.ajouterCombo(combosTries[i]);
        }
        return topRange;
    }
    private int[] getCombosTries() {
        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
        CalculatriceEquite calculatriceEquite = new CalculatriceEquite(configCalculatrice);

        List<ComboReel> tousLesCombos = GenerateurCombos.combosReels;

        List<RangeReelle> rangeVillains = new ArrayList<>();
        RangeReelle rangePleine = new RangeReelle();
        rangePleine.remplir();
        rangeVillains.add(rangePleine);

        Board boardVide = new Board();

        List<EquiteCombo> combosAvecEquite = new ArrayList<>();

        for (ComboReel combo : tousLesCombos) {
            float equite = calculatriceEquite.equiteGlobaleMain(combo, boardVide, rangeVillains);
            EquiteCombo nouveauCombo = new EquiteCombo(combo, equite);
            combosAvecEquite.add(nouveauCombo);
        }

        List<EquiteCombo> combosEquitesTries = combosAvecEquite.stream()
                .sorted(Comparator.comparingDouble(EquiteCombo::getEquite).reversed())
                .collect(Collectors.toList());

        int[] combosTries = new int[combosEquitesTries.size()];
        for (int i = 0; i < combosEquitesTries.size(); i++) {
            combosTries[i] = combosEquitesTries.get(i).combo.toInt();
            System.out.println(combosEquitesTries.get(i).combo);
        }

        return combosTries;

    }

    private static class EquiteCombo {
        protected ComboReel combo;
        protected float equite;

        protected EquiteCombo(ComboReel combo, float equite) {
            this.combo = combo;
            this.equite = equite;
        }

        public float getEquite() {return this.equite;}
    }

    public static void main(String[] args) {
        GenerateurRange generateurRange = new GenerateurRange();
        ComboReel comboReel = new ComboReel('T', 'h', 'T', 's');
        Board boardVide = new Board();
        List<RangeReelle> rangeVillain = new ArrayList<>();
        rangeVillain.add(generateurRange.topRange(0.15f));

        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modePrecision();
        CalculatriceEquite calculatriceEquite = new CalculatriceEquite(configCalculatrice);
        float equite = calculatriceEquite.equiteGlobaleMain(comboReel, boardVide, rangeVillain);

        System.out.println(equite);
    }
}
