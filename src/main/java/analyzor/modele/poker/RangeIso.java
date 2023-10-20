package analyzor.modele.poker;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.SituationIso;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class RangeIso extends RangeSauvegardable {

    // seulement besoin de persister la range
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ComboIso> combos = new ArrayList<>();

    //constructeurs
    /*
    soit créée à vide
    soit récupéré depuis BDD
     */
    public RangeIso() {};

    public  void remplir() {

    }
}


