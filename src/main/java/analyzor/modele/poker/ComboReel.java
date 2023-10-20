package analyzor.modele.poker;

import analyzor.modele.utils.Bits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ComboReel {
    /*
    soit créée à vide
    soit récupéré depuis BDD
     */
    //on va toujours en avoir besoin pour le calcul
    private final List<Carte> cartesReelles;
    private static final int MASQUE_CARTE = Bits.creerMasque(Carte.N_BITS_CARTE, Carte.N_BITS_CARTE);
    private final int comboBits;

    public ComboReel(List<Carte> cartesJoueur) {
        assert cartesJoueur.size() == 2;
        cartesReelles = cartesJoueur;
        comboBits = (cartesJoueur.get(0).toInt()
                >> Carte.N_BITS_CARTE) | cartesJoueur.get(1).toInt();
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
}
