package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Entree {
    @Id
    private Long id;

    //ACTION
    @JoinColumn(nullable = false)
    private int numAction;
    /*
    deprecated
    private String bloc;
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Action action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private Action actionIso;

    private float value;

    // SITUATION
    // la suppression des entrées entraine la suppression des tours correspondants
    // quand on enregistre la main, on enregistre aussi le tour
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private TourMain tourMain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Situation situation;

    // créé lors de l'import -> utile pour mapper rapidement l'arbre théorique sur les entrées
    private Long idNoeudTheorique;

    @JoinColumn(nullable = false)
    private float stackEffectifBB;

    // Infos joueur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Joueur joueur;

    private int cartesJoueur;
    @JoinColumn(nullable = false)
    private float stackJoueurBB;

    // POT
    @JoinColumn(nullable = false)
    private float ancienPotBB;
    @JoinColumn(nullable = false)
    private float potActuelBB;
    @JoinColumn(nullable = false)
    private float montantCall;
    @JoinColumn(nullable = false)
    private float potBounty;


    //constructeurs
    public Entree() {}

    public Entree(
            int numAction,
            Action action,
            TourMain tourMain,
            Situation situation,
            float stackEffectifBB,
            Joueur joueur,
            int cartesJoueur,
            float stackJoueurBB,
            float ancienPotBB,
            float potActuelBB,
            float montantCall,
            float potBounty
    ) {
        this.numAction = numAction;
        this.action = action;
        this.tourMain = tourMain;
        this.situation = situation;
        this.stackEffectifBB = stackEffectifBB;
        this.joueur = joueur;
        this.cartesJoueur = cartesJoueur;
        this.stackJoueurBB = stackJoueurBB;
        this.ancienPotBB = ancienPotBB;
        this.potActuelBB = potActuelBB;
        this.montantCall = montantCall;
        this.potBounty = potBounty;

        genererId();
    }

    private void genererId() {
        this.id = (tourMain.getId() << 6) + numAction;
    }

    //getters, setters

    private long getId() {
        return id;
    }

    public void setValue(float value) {
        this.value = value;
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


    public Joueur getJoueur() {
        return joueur;
    }

    public Situation getSituation() {
        return situation;
    }

    public float getStackEffectif() {
        return stackEffectifBB;
    }

    public float getPotTotal() {
        return ancienPotBB + potActuelBB;
    }

    public float getPotBounty() {
        return potBounty;
    }

    public Long getIdNoeudTheorique() {
        return idNoeudTheorique;
    }
}
