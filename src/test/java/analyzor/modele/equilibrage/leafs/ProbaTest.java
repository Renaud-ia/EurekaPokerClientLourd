package analyzor.modele.equilibrage.leafs;

import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.evaluation.EquiteFuture;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProbaTest {
    int PAS = 10;
    @Test
    void testerProbas() {
        List<ComboDenombrable> comboDenombrables = echantillonCombosDenombrables();
        ProbaEquilibrage probaEquilibrage = new ProbaEquilibrage(5326, PAS);

        for (ComboDenombrable comboDenombrable : comboDenombrables) {
            probaEquilibrage.calculerProbas(comboDenombrable);
            comboDenombrable.initialiserStrategie();
            System.out.println("STRATEGIE : " + Arrays.toString(comboDenombrable.getStrategie()));
        }
    }


    List<ComboDenombrable> echantillonCombosDenombrables() {
        // exemples construit sur la base de data ROOT 23BB 2P SPIN
        List<ComboDenombrable> comboDenombrables = new ArrayList<>();

        DenombrableIso denombrableAA =
                construireDenombrableIso("AA", 0.004524f, new int[] {0, 4, 0, 6},
                        new double[] {0.115830116f, 0.29199314f, 0.2541422f, 0.51597935f});

        comboDenombrables.add(denombrableAA);

        DenombrableIso denombrableKJo =
                construireDenombrableIso("KJo", 0.0090497744f, new int[] {1, 1, 0, 3},
                        new double[] {0.115830116f, 0.22171624f, 0.19297525f, 0.39179343f});
        comboDenombrables.add(denombrableKJo);

        DenombrableIso denombrableQ2s =
                construireDenombrableIso("Q2s", 0.0030165914, new int[] {0, 1, 0, 1},
                        new double[] {0.115830116, 0.17326018, 0.15080054, 0.30616704});
        comboDenombrables.add(denombrableQ2s);

        DenombrableIso denombrable73o =
                construireDenombrableIso("73o", 0.0090497744, new int[] {0, 0, 0, 1},
                        new double[] {0.115830116, 0.1361463, 0.11849772, 0.24058333});
        comboDenombrables.add(denombrable73o);

        return comboDenombrables;
    }

    DenombrableIso construireDenombrableIso(String nomCombo, double pCombo, int[] observations, double[] showdowns) {
        int nActions = observations.length;

        ComboIso comboIso = new ComboIso(nomCombo);
        EquiteFuture equiteFuture = new EquiteFuture(3);
        DenombrableIso denombrableIso = new DenombrableIso(comboIso, (float) pCombo, equiteFuture, nActions);
        denombrableIso.setPas(PAS);
        for (int i = 0; i < nActions; i++) {
            denombrableIso.setObservation(i, observations[i]);
            denombrableIso.setShowdown(i, (float) showdowns[i]);
        }

        return denombrableIso;
    }
}
