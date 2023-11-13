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

    private float value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private TourMain tourMain;


    // créé lors de l'import -> utile pour mapper rapidement l'arbre théorique sur les entrées
    private Long idNoeudTheorique;
    private float betSize;

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
    private float potBounty;


    //constructeurs
    public Entree() {}

    public Entree(
            int numAction,
            TourMain tourMain,
            long idNoeudTheorique,
            float betSize,
            float stackEffectifBB,
            Joueur joueur,
            int cartesJoueur,
            float stackJoueurBB,
            float ancienPotBB,
            float potActuelBB,
            float potBounty
    ) {
        this.numAction = numAction;
        this.tourMain = tourMain;
        this.idNoeudTheorique = idNoeudTheorique;
        this.betSize = betSize;
        this.stackEffectifBB = stackEffectifBB;
        this.joueur = joueur;
        this.cartesJoueur = cartesJoueur;
        this.stackJoueurBB = stackJoueurBB;
        this.ancienPotBB = ancienPotBB;
        this.potActuelBB = potActuelBB;
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
