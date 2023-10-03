package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Entree {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //ACTION
    private int numAction;
    /*
    deprecated
    private String bloc;
     */
    private int numActionJoueur;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Action action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private Action actionIso;

    private float value;

    // SITUATION
    // la suppression des entrées entraine la suppression des tours correspondants
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(nullable = false)
    private TourMain tourMain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Situation situation;

    private float stackEffectifBB;

    // Infos joueur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Joueur joueur;

    private int cartesJoueur;
    private float stackJoueurBB;

    // POT
    private float ancienPotBB;
    private float potActuelBB;
    private float montantCall;
    private float potBouty;


    //constructeurs
    public Entree() {}

    //getters, setters

    private long getId() {
        return id;
    }


    // recommandé de réécrire equals et hashCode quand relation réciproque
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entree )) return false;
        return id != null && id.equals(((Entree) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
