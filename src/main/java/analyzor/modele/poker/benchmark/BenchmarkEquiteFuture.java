package analyzor.modele.poker.benchmark;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.poker.RangeReelle;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.modele.poker.evaluation.EquiteFuture;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkEquiteFuture {
    public static void main(String[] args) {
        final String nomComboTest = "AKo";
        final String nomComboProche = "AQo";
        final int iterations = 10;

        ComboIso comboIso = new ComboIso(nomComboTest);
        ComboReel randomCombo = comboIso.toCombosReels().getFirst();
        ComboReel comboProche = new ComboIso(nomComboProche).toCombosReels().getFirst();

        Board board = new Board();
        List<RangeReelle> rangesVillains = new ArrayList<>();
        RangeReelle range = new RangeReelle();
        range.remplir();
        rangesVillains.add(range);

        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeExact();

        CalculatriceEquite calculatriceEquite = new CalculatriceEquite(configCalculatrice);

        List<EquiteFuture> equiteFutures = new ArrayList<>();
        List<Double> tempsCalcul = new ArrayList<>();

        System.out.println("######TEST DISTANCE EQUITE FUTURE DE " + nomComboTest + "########");
        System.out.println("Nombre d'itérations : " + iterations);

        for (int i = 0; i < iterations; i++) {
            System.out.println("ITERATION N°" + i);
            long startTime = System.nanoTime();
            EquiteFuture equiteFuture = calculatriceEquite.equiteFutureMain(randomCombo, board, rangesVillains);
            long endTime = System.nanoTime();
            double dureeMS = (endTime - startTime) / 1_000_000.0;

            equiteFutures.add(equiteFuture);
            tempsCalcul.add(dureeMS);

        }

        EquiteFuture equiteComboProche = calculatriceEquite.equiteFutureMain(comboProche, board, rangesVillains);
        System.out.println("Distance avec " + nomComboProche + " : " + equiteComboProche.distance(equiteFutures.getFirst()));
        System.out.println("Equité future : " + equiteComboProche);

        float distanceMoyenne = distanceMoyenne(equiteFutures);
        float ecartType = ecartType(equiteFutures, distanceMoyenne);
        float dureeMoyenne = dureeMoyenne(tempsCalcul);

        System.out.println("Distance moyenne : " + distanceMoyenne);
        System.out.println("Ecart type : " + ecartType);
        System.out.println("Durée moyenne : " + dureeMoyenne);
    }

    private static float ecartType(List<EquiteFuture> equiteFutures, float distanceMoyenne) {
        float variance = 0;
        for (EquiteFuture equite1 : equiteFutures) {
            for (EquiteFuture equite2 : equiteFutures) {
                if (equite1 == equite2) continue;

                float distance = equite1.distance(equite2);
                variance += (float) Math.pow(distance - distanceMoyenne, 2);
            }
        }

        return (float) Math.sqrt(variance);
    }

    private static float dureeMoyenne(List<Double> tempsCalcul) {
        double dureeMoyenne = 0;
        for (double duree : tempsCalcul) {
            dureeMoyenne += duree;
        }

        dureeMoyenne /= tempsCalcul.size();

        return (float) dureeMoyenne;
    }

    private static float distanceMoyenne(List<EquiteFuture> equiteFutures) {
        float distanceMoyenne = 0;
        int compte = 0;
        for (EquiteFuture equite1 : equiteFutures) {
            for (EquiteFuture equite2 : equiteFutures) {
                if (equite1 == equite2) continue;

                distanceMoyenne += equite1.distance(equite2);
                compte++;
            }
        }

        return distanceMoyenne / compte;
    }
}
