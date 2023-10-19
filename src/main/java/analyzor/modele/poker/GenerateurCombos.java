package analyzor.modele.poker;

import java.util.ArrayList;
import java.util.List;

public class GenerateurCombos {
    private static Character[] suffixesIso = {'o', 's'};
    public List<ComboIso> getCombosIso() {
        List<ComboIso> combos = new ArrayList<>();
        int index = 0;
        for (Character rank1 : Carte.STR_RANKS) {
            for (Character rank2 : Carte.STR_RANKS) {
                for (Character suffix : suffixesIso) {
                    String nomCombo = String.valueOf(rank1 + rank2 + suffix);
                    combos.add(new ComboIso(nomCombo));
                }
            }
            index++;
        }
        return combos;
    }
}
