package analyzor.modele.poker;

import org.w3c.dom.ranges.Range;

public class RangeReelle {
    /**
    utilisée seulement pour calcul d'équité ?

    Stocke les combos réels d'une range (=1326) sous forme d'un index binaire
                         CARTE :
                        bxRRRRSS
                1) R = 4 bits pour codage rank
                2) S = 2 bits pour codage suit

    Indexation ultra rapide, conversion facile dans les deux sens
    Possibilité de supprimer des cartes et les combos associés
    Tirage au sort de combos au hasard
    Sensible à l'ordre des cartes => trier les combos en amont
    Range doit être exprimée en % du combo
     */

    private final int[] combos = new int[(Carte.CARTE_MAX << Carte.N_BITS_CARTE) | Carte.CARTE_MAX];

    public RangeReelle() {
        /**
         création d'une range pleine
         */
    }

    public RangeReelle(RangeIso rangeIso) {
        //TODO
    }

    public RangeReelle(RangeDynamique rangeDynamique) {
        //TODO
    }
}
