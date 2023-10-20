package analyzor.modele.poker;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.SituationIso;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ComboIso  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int ComboInteger;
    private float value;
    @Transient
    String nomCombo;

    public ComboIso(String nomCombo) {
        System.out.println(nomCombo);
    }

    //TODO : quel intérêt????
    public ComboIso(List<Carte> cartesJoueur) {
    }

    public List<Integer> toCombosReels() {
        return new ArrayList<>();
    }

}
