package analyzor.modele.poker;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class RangeIso extends RangeSauvegardable implements RangeDenombrable {

    // seulement besoin de persister la range
    // on veut récupérer tous les combos avec la range
    //todo : est ce qu'on veut orphan removal???
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="range")
    private List<ComboIso> combos = new ArrayList<>();

    //constructeurs
    /*
    soit créée à vide
    soit récupéré depuis BDD
     */
    public RangeIso() {
        remplir();
    };

    private void remplir() {
        for (ComboIso combo : GenerateurCombos.combosIso) {
            combo.setValeur(1);
            this.combos.add(combo);
        }
    }

    public List<ComboIso> getCombos() {
        return combos;
    }
}


