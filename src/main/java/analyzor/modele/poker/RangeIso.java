package analyzor.modele.poker;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
public class RangeIso extends RangeSauvegardable implements RangeDenombrable {

    // seulement besoin de persister la range
    // on veut récupérer tous les combos avec la range
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="range")
    private List<ComboIso> combos = new ArrayList<>();

    // pour opérations rapides sur range
    @Transient
    private HashMap<ComboIso, Integer> indexCombo;

    //constructeurs
    /*
    soit créée à vide
    soit récupéré depuis BDD
     */
    public RangeIso() {
    }

    public void remplir() {
        // todo remplir la hashMap + hashcode dans Combo Iso
        for (ComboIso combo : GenerateurCombos.combosIso) {
            combo.setValeur(1);
            this.combos.add(combo);
        }
    }

    public List<ComboIso> getCombos() {
        return combos;
    }

    public float getValeur(ComboIso comboIso) {
        for (ComboIso comboRange : this.combos) {
            if (comboRange.equals(comboIso)) return comboRange.getValeur();
        }
        return 0f;
    }

    public void incrementerCombo(ComboIso comboIso, float valeur) {
        for (ComboIso comboRange : this.combos) {
            if (comboRange.equals(comboIso)) {
                comboRange.incrementer(valeur);
                return;
            }
        }

        // pas trouvé on le crée
        ComboIso nouveauCombo = comboIso.copie();
        nouveauCombo.setValeur(valeur);
        this.combos.add(nouveauCombo);
    }

    public void multiplier(RangeIso rangeIso) {
        for (ComboIso comboMultiplie : rangeIso.combos) {
            for (ComboIso comboRange : this.combos) {
                if (comboRange.equals(comboMultiplie)) {
                    comboRange.multiplier(comboMultiplie.getValeur());
                    return;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringRange = new StringBuilder();
        stringRange.append("[RANGE ISO : ");
        for (ComboIso comboIso : this.combos) {
            stringRange.append(comboIso.nomCombo);
            stringRange.append(":");
            stringRange.append(comboIso.getValeur());
            stringRange.append(" ,");
        }
        stringRange.append("]");

        return stringRange.toString();
    }

    public ComboIso getCombo(ComboIso comboCherche) {
        for (ComboIso comboIsoRange : combos) {
            if (comboCherche.equals(comboIsoRange)) {
                return comboIsoRange;
            }
        }
        return null;
    }

    public void ajouterCombo(ComboIso combo) {
        this.combos.add(combo);
    }

    // nombre de combos dans la range
    public float nCombos() {
        float nCombos = 0;
        for (ComboIso comboIso : combos) {
            nCombos += comboIso.getNombreCombos() * comboIso.getValeur();
        }

        return nCombos;
    }
}


