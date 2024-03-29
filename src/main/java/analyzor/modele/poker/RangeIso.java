package analyzor.modele.poker;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
public class RangeIso extends RangeSauvegardable implements RangeDenombrable {



    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="range")
    private List<ComboIso> combos = new ArrayList<>();


    @Transient
    private HashMap<ComboIso, Integer> mapCombos;


    
    public RangeIso() {
    }

    public void remplir() {
        for (ComboIso combo : GenerateurCombos.combosIso) {
            ComboIso comboCopie = combo.copie();
            comboCopie.setValeur(1);
            this.combos.add(comboCopie);
        }
    }


    public void rangeVide() {
        for (ComboIso combo : GenerateurCombos.combosIso) {
            ComboIso comboCopie = combo.copie();
            comboCopie.setValeur(0);
            this.combos.add(comboCopie);
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


        ComboIso nouveauCombo = comboIso.copie();
        nouveauCombo.setValeur(valeur);
        this.combos.add(nouveauCombo);
    }

    public void multiplier(RangeIso rangeIso) {

        for (ComboIso comboMultiplie : rangeIso.combos) {
            for (ComboIso comboRange : this.combos) {
                if (comboRange.equals(comboMultiplie)) {
                    comboRange.multiplier(comboMultiplie.getValeur());

                    break;
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


    public float nCombos() {
        float nCombos = 0;
        for (ComboIso comboIso : combos) {
            nCombos += comboIso.getNombreCombos() * comboIso.getValeur();
        }

        return nCombos;
    }


    public RangeIso copie() {
        RangeIso rangeCopiee = new RangeIso();
        for (int i = combos.size() -1; i >= 0; i--) {
            ComboIso comboIso = this.combos.get(i);
            ComboIso comboCopie = comboIso.copie();
            rangeCopiee.combos.add(comboCopie);
        }

        return rangeCopiee;
    }
}


