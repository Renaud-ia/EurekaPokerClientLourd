package analyzor.modele.arbre.noeuds;

import analyzor.modele.parties.Entree;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class NoeudPreflop extends NoeudArbre {
    private boolean leaf;
    @ManyToOne
    @JoinColumn(name = "noeudPrecedent_id")
    private NoeudArbre noeudPrecedent;
    @OneToMany(mappedBy = "noeudPrecedent")
    private Set<NoeudArbre> noeudsSuivants;

    @OneToMany(mappedBy = "situation")
    private List<Entree> entrees = new ArrayList<>();

    public NoeudPreflop() {};
}
