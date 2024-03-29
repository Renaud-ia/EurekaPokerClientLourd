package analyzor.modele.poker;

import analyzor.modele.utils.Bits;

import java.util.*;


public class ComboReel {

    
    private final List<Carte> cartesReelles;
    private static final int MASQUE_CARTE = (int) Bits.creerMasque(Carte.N_BITS_CARTE, Carte.N_BITS_CARTE);
    private final int comboBits;

    public ComboReel(List<Carte> cartesJoueur) {
        assert cartesJoueur.size() == 2;


        
        if (cartesJoueur.get(0).toInt() > cartesJoueur.get(1).toInt()) {
            comboBits = (cartesJoueur.get(0).toInt()
                    << Carte.N_BITS_CARTE) | cartesJoueur.get(1).toInt();
        }
        else {
            comboBits = (cartesJoueur.get(1).toInt()
                    << Carte.N_BITS_CARTE) | cartesJoueur.get(0).toInt();
            Collections.reverse(cartesJoueur);
        }
        cartesReelles = cartesJoueur;
    }

    public ComboReel (int comboInt) {
        Carte carte1 = new Carte(comboInt >> Carte.N_BITS_CARTE);
        Carte carte2 = new Carte(comboInt & MASQUE_CARTE);
        cartesReelles = new ArrayList<>(Arrays.asList(carte1, carte2));
        this.comboBits = comboInt;
    }

    public ComboReel(Character rank1, Character suit1, Character rank2, Character suit2) {
        this(Arrays.asList(new Carte(rank1, suit1), new Carte(rank2, suit2)));
    }

    public int toInt() {
        return comboBits;
    }


    public List<Carte> getCartes() {
        return cartesReelles;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        else if (!(o instanceof ComboReel)) return false;
        return (((ComboReel) o).comboBits == this.comboBits);
    }

    @Override
    public int hashCode() {
        return comboBits;
    }

    @Override
    public String toString() {
        return "Combo [" + cartesReelles.get(0) + ", " + cartesReelles.get(1) + "] : " + comboBits;
    }

    int getPremierRang() {
        return cartesReelles.get(0).getIntRank();
    }

    int getSecondRang() {
        return cartesReelles.get(1).getIntRank();
    }

    int getPremierSuit() {
        return cartesReelles.get(0).getIntSuit();
    }

    int getSecondSuit() {
        return cartesReelles.get(1).getIntSuit();
    }
}
