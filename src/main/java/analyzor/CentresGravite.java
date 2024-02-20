package analyzor;

import analyzor.modele.denombrement.CalculEquitePreflop;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.TourMain;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.GenerateurCombos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CentresGravite {
    public static void main(String[] args) {
        List<ComboIso> combosIso = GenerateurCombos.combosIso;
        List<ComboIso> centresGravite = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Random random = new Random();
            int randomIndex = random.nextInt(0, combosIso.size());

            ComboIso comboRandom = combosIso.get(randomIndex);

            centresGravite.add(comboRandom);
        }

        System.out.println(centresGravite);
    }
}
