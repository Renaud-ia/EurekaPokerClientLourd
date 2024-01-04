package analyzor.modele.simulation;

import analyzor.modele.poker.RangeIso;

/**
 * classe qui représente une range condensée visible
 * permet de ne pas exposer RangeIso au controleur
 */
public class RangeCondensee {
    private final RangeIso rangeIso;
    RangeCondensee(RangeIso rangeIso) {
        this.rangeIso = rangeIso;
    }



    public class ComboCondense {

    }
}
