package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Entree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @JoinColumn(nullable = false)
    private int numAction;

    private float resultat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private TourMain tourMain;


    
    @Column(nullable = false)
    private Long idNoeudTheorique;
    @Column(nullable = false)
    private float betSize;

    @JoinColumn(nullable = false)
    private long codeStackEffectif;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Joueur joueur;

    
    private int cartesJoueur;
    @Column(nullable = false)
    private float stackJoueurBB;

    
    @Column(nullable = false)
    private float potActuelBB;
    @Column(nullable = false)
    private float potBounty;


    
    public Entree() {}

    public Entree(
            int numAction,
            TourMain tourMain,
            long idNoeudTheorique,
            float betSize,
            long codeStackEffectif,
            Joueur joueur,
            float stackJoueurBB,
            float potActuelBB,
            float potBounty
    ) {
        this.numAction = numAction;
        this.tourMain = tourMain;
        this.idNoeudTheorique = idNoeudTheorique;
        this.betSize = betSize;
        this.codeStackEffectif = codeStackEffectif;
        this.joueur = joueur;
        this.stackJoueurBB = stackJoueurBB;
        this.potActuelBB = potActuelBB;
        this.potBounty = potBounty;
    }

    

    public long getId() {
        return id;
    }

    public void setValue(float value) {
        this.resultat = value;
    }


    
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

    public long getCodeStackEffectif() {
        return codeStackEffectif;
    }

    public float getPotTotal() {
        return potActuelBB;
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

    public void setCartes(int cartesJoueur) {
        this.cartesJoueur = cartesJoueur;
    }

    public int getCombo() {
        return cartesJoueur;
    }

    public int getNumAction() {
        return numAction;
    }
}
