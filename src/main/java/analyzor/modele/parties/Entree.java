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
    private String bloc;
    private int nActionJoueurs;
    private int betSize;
    private float value;

    // SITUATION
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private TourMain tourMain;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private Situation situation;

    private float stackEffectifBB;

    // Infos joueur
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
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
