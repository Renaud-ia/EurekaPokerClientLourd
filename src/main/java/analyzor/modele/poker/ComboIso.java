package analyzor.modele.poker;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.SituationIso;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ComboIso  {
    private int ComboInteger;
    private float value;

    public ComboIso(String nomCombo) {
        //TODO à coder
    }

    //TODO : quel intérêt????
    public ComboIso(List<Carte> cartesJoueur) {
    }

    public List<Integer> toCombosReels() {
        return new ArrayList<>();
    }

}
