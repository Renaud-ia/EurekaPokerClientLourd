package analyzor.modele.poker;

import analyzor.modele.utils.Bits;

import java.util.List;

public class ComboReel {/*
    soit créée à vide
    soit récupéré depuis BDD
     */
    private static int MASQUE_CARTE;
    private final int comboBits;
    static {
        MASQUE_CARTE = Bits.creerMasque(Carte.N_BITS_CARTE, Carte.N_BITS_CARTE);
    }
    public ComboReel(List<Carte> cartesJoueur) {
        assert cartesJoueur.size() == 2;
        comboBits = (cartesJoueur.get(0).toInt()
                >> Carte.N_BITS_CARTE) | cartesJoueur.get(1).toInt();
    }

    public ComboReel (int comboInt) {
        this.comboBits = comboInt;
    }

    public int toInt() {
        return comboBits;
    }


}
