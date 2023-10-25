package analyzor.modele.poker;

import analyzor.modele.utils.Combinations;
import analyzor.modele.utils.Permutations;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ComboIso  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private float valeur;
    //todo : veut dire que ça sera stocké en clair dans la BDD mais est-ce grave ???
    String nomCombo;
    @Transient
    static final Character[] suffixesIso = {'o', 's'};
    @Transient
    private static final List<List<Character>> combinationsSuits;
    @Transient
    private static final List<List<Character>> permutationsSuits;

    static {
        Combinations<Character> combinator = new Combinations<>(Carte.STR_SUITS);
        combinationsSuits = combinator.getCombinations(2);

        Permutations<Character> suitPermutations = new Permutations<>();
        permutationsSuits = suitPermutations.generate(Carte.STR_SUITS, 2);
    }

    public ComboIso(String nomCombo) {
        this.nomCombo = nomCombo;
    }

    /**
     * convertit un ComboIso en liste de ComboReel
     * @return la liste des ComboReel
     */
    public List<ComboReel> toCombosReels() {
        //TODO OPTIMISATION : fait doublon avec Generateur Combos à voir comment fusionner
        List<ComboReel> listCombosReels = new ArrayList<>();
        char rank1 = nomCombo.charAt(0);
        char rank2 = nomCombo.charAt(1);

        if (nomCombo.length() == 2) {
            for (List<Character> suits : combinationsSuits) {
                Character suit1 = suits.get(0);
                Character suit2 = suits.get(1);

                ajouterComboReel(rank1, rank2, suit1, suit2, listCombosReels);
            }
        }

        else if (nomCombo.charAt(2) == 'o') {

            for(List<Character> suits : permutationsSuits) {
                Character suit1 = suits.get(0);
                Character suit2 = suits.get(1);

                ajouterComboReel(rank1, rank2, suit1, suit2, listCombosReels);
            }
        }

        else if (nomCombo.charAt(2) == 's') {
            for (Character suit : Carte.STR_SUITS) {
                ajouterComboReel(rank1, rank2, suit, suit, listCombosReels);
            }
        }

        else throw new IllegalArgumentException("Format de combo non reconnu");

        return listCombosReels;
    }

    private void ajouterComboReel(char rank1, char rank2, char suit1, char suit2, List<ComboReel> list) {
        List<Carte> cartesCombo = new ArrayList<>();
        cartesCombo.add(new Carte(rank1, suit1));
        cartesCombo.add(new Carte(rank2, suit2));
        list.add(new ComboReel(cartesCombo));
    }

    public float getValeur() {
        return valeur;
    }

    public void setValeur(int valeur) {
        this.valeur = valeur;
    }

    @Override
    public String toString() {
        return "Combo Iso (" + nomCombo + ")";
    }
}
