package analyzor.modele.poker;

import analyzor.modele.utils.Combinations;

import java.util.ArrayList;
import java.util.List;

public class GenerateurCombos {
    private static final List<Character[]> pairesRanksTriees = getPairesRanksTriees();
    public static final List<ComboReel> combosReels = getCombosReels();
    public static final List<ComboIso> combosIso = getCombosIso();
    public static final List<Carte> toutesLesCartes = getToutesLesCartes();

    private static List<Carte> getToutesLesCartes() {
        List<Carte> toutesLesCartes = new ArrayList<>();
        for (Character rank : Carte.STR_RANKS) {
            for (Character suit : Carte.STR_SUITS) {
                toutesLesCartes.add(new Carte(rank, suit));
            }
        }
        return toutesLesCartes;
    }

    private static List<ComboIso> getCombosIso() {
        List<ComboIso> combos = new ArrayList<>();
        for (Character[] paireRank : pairesRanksTriees) {
            Character rank1 = paireRank[0];
            Character rank2 = paireRank[1];
            for (Character suffix : ComboIso.suffixesIso) {
                String nomCombo = rank1.toString() + rank2.toString() + suffix;
                combos.add(new ComboIso(nomCombo));
            }
        }
        for (Character rank : Carte.STR_RANKS) {
            String pocketPaire = rank.toString() + rank.toString();
            combos.add(new ComboIso(pocketPaire));
        }

        return combos;
    }

    private static List<ComboReel> getCombosReels() {
        List<ComboReel> combos = new ArrayList<>();
        for (Character[] paireRank : pairesRanksTriees) {
            Character rank1 = paireRank[0];
            Character rank2 = paireRank[1];
            for (Character suit1 : Carte.STR_SUITS) {
                for (Character suit2 : Carte.STR_SUITS) {
                    if (!(rank1 == rank2 && suit1 == suit2)) {
                        combos.add(new ComboReel(rank1, suit1, rank2, suit2));
                    }
                }
            }
        }
        for (Character rank : Carte.STR_RANKS) {
            Combinations<Character> combinator = new Combinations<>(Carte.STR_SUITS);
                for (List<Character> suits : combinator.getCombinations(2)) {
                    Character suit1 = suits.get(0);
                    Character suit2 = suits.get(1);
                    combos.add(new ComboReel(rank, suit1, rank, suit2));
            }
        }
        return combos;
    }

    private static List<Character[]> getPairesRanksTriees() {
        // garantit l'ordre des ranks lors de la génération (AK et pas KA)
        List<Character[]> listPairesRanks = new ArrayList<>();
        int index = 0;
        for (Character rank1 : Carte.STR_RANKS) {
            for (int i = index - 1; i >= 0; i--) {
                Character rank2 = Carte.STR_RANKS[i];
                Character[] paireRank = {rank1, rank2};
                listPairesRanks.add(paireRank);
            }
            index++;
        }

        return listPairesRanks;
    }
}
