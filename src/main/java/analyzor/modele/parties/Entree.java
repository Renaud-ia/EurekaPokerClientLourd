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
    @Column(nullable = false)
    private Long idNoeudTheorique;
    @Column(nullable = false)
    private float betSize;

    @JoinColumn(nullable = false)
    private float stackEffectifBB;

    // Infos joueur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Joueur joueur;

    // vaudra 0 si pas de cartes
    private int cartesJoueur;
    @Column(nullable = false)
    private float stackJoueurBB;

    // POT
    @Column(nullable = false)
    private float ancienPotBB;
    @Column(nullable = false)
    private float potActuelBB;
    @Column(nullable = false)
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
        // todo attention risque de collision quand partie enregistrée sur deux fichiers
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

    public float getBetSize() {
        return betSize;
    }

    public int getCartesJoueur() {
        return cartesJoueur;
    }

    public TourMain getTourMain() {
        return tourMain;
    }

    public int getIdAction() {
        return numAction;
    }
}
