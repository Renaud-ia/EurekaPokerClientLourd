package analyzor.modele.poker;

import analyzor.modele.utils.Combinations;
import analyzor.modele.utils.Permutations;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class ComboIso {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private float valeur;
    //todo : veut dire que ça sera stocké en clair dans la BDD mais est-ce grave ???
    String nomCombo;
    // important doit rester dans cet ordre
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

    // pour Hibernate
    public ComboIso() {}

    // important, il faut que ce soit compatible avec codeReduit()
    public ComboIso(String nomCombo) {
        this.nomCombo = nomCombo;
    }

    public ComboIso(String nomCombo, float valeur) {
        this.nomCombo = nomCombo;
        this.valeur = valeur;
    }

    public ComboIso(ComboReel comboObserve) {
        int rank1 = comboObserve.getPremierRang();
        int rank2 = comboObserve.getSecondRang();
        int suit1 = comboObserve.getPremierSuit();
        int suit2 = comboObserve.getSecondSuit();

        StringBuilder stringCombo = new StringBuilder();
        stringCombo.append(Carte.INT_RANK_TO_CHAR_RANK.get(rank1));
        stringCombo.append(Carte.INT_RANK_TO_CHAR_RANK.get(rank2));

        if (rank1 == rank2) {
        }
        else if (suit1 == suit2) {
            stringCombo.append(suffixesIso[1]);
        }
        else stringCombo.append(suffixesIso[0]);

        this.nomCombo = stringCombo.toString();
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
            for (List<Character> suits : permutationsSuits) {
                Character suit1 = suits.get(0);
                Character suit2 = suits.get(1);

                ajouterComboReel(rank1, rank2, suit1, suit2, listCombosReels);
                ajouterComboReel(rank1, rank2, suit2, suit1, listCombosReels);
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

    public void setValeur(float valeur) {
        this.valeur = valeur;
    }

    public ComboIso copie() {
        return new ComboIso(this.nomCombo, this.valeur);
    }

    public void incrementer(float valeur) {
        this.valeur += valeur;
    }

    public void multiplier(float valeur) {
        this.valeur *= valeur;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ComboIso)) return false;
        return this.intComboIso() == ((ComboIso) o).intComboIso();
    }

    @Override
    public int hashCode() {
        return intComboIso();
    }

    private int intComboIso() {
        int intComboIso = 0;
        char rank1 = this.nomCombo.charAt(0);
        char rank2 = this.nomCombo.charAt(1);
        intComboIso += (intComboIso << Carte.N_BITS_RANK) + rank1;
        intComboIso += (intComboIso << Carte.N_BITS_RANK) + rank2;

        int N_BITS_SUFFIXE = 2;
        if (nomCombo.length() == 2) {
            intComboIso += (intComboIso << N_BITS_SUFFIXE) + 1;
        }
        else if (nomCombo.charAt(2) == 'o') {
            intComboIso += (intComboIso << N_BITS_SUFFIXE) + 2;
        }
        else if (nomCombo.charAt(2) == 's') {
            intComboIso += (intComboIso << N_BITS_SUFFIXE) + 3;
        }
        else throw new IllegalArgumentException("Nom du combo pas reconnu : " + nomCombo);

        return intComboIso;

    }

    @Override
    public String toString() {
        return "Combo Iso (" + nomCombo + ")";
    }

    // important : cette méthode sert pour enregistrer les valeurs dans BerkeleyDB + pour la vue
    public String codeReduit() {
        return nomCombo;
    }

    public String strCompacte() {
        return nomCombo;
    }

    public int getNombreCombos() {
        return this.toCombosReels().size();
    }
}
