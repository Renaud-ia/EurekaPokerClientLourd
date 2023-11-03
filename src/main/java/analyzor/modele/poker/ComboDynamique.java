package analyzor.modele.poker;

import analyzor.modele.arbre.noeuds.NoeudArbre;
import analyzor.modele.parties.Action;
import jakarta.persistence.*;

@Entity
public class ComboDynamique {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int equiteFlop;
    private int equiteTurn;
    private int equiteRiver;

    @ManyToOne
    private NoeudArbre situation;

    @ManyToOne
    private Action action;

    // constructeurs
    public ComboDynamique() {}
    public ComboDynamique(HistogrammeEquite histogrammeEquite) {

    }
}
