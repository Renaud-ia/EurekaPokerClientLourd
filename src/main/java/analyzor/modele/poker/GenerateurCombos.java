package analyzor.modele.poker;

import java.util.ArrayList;
import java.util.List;

public class GenerateurCombos {
    private static final Character[] suffixesIso = {'o', 's'};

    public static List<ComboIso> getCombosIso() {
        List<ComboIso> combos = new ArrayList<>();
        int index = 0;
        for (Character rank1 : Carte.STR_RANKS) {
            for (int i = index - 1; i >= 0; i--) {
                Character rank2 = Carte.STR_RANKS[i];
                for (Character suffix : suffixesIso) {
                    String nomCombo = rank1.toString() + rank2.toString() + suffix;
                    combos.add(new ComboIso(nomCombo));
                }
            }
            String pocketPaire = rank1.toString() + rank1.toString();
            combos.add(new ComboIso(pocketPaire));
            index++;
        }

        return combos;
    }

    public List<ComboReel> getCombosReels() {
        //todo on demande à comboIso les combos réels ?
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        List<ComboIso> combos = GenerateurCombos.getCombosIso();
        System.out.println(combos.size());
    }
}
