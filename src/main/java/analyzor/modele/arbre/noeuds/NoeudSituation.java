package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.ProfilJoueur;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * on a besoin d'un noeud Situation qui encapsule les NoeudsAction
 * d'une part évite de stocker deux fois la même info
 * d'autre part, indispensable pour Simulation car les actions possibles vont dépendre du NoeudSituation
 */
@Entity
public class NoeudSituation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private final ProfilJoueur profilJoueur;

    @ManyToOne
    @JoinColumn(nullable = false)
    private final FormatSolution formatSolution;
    // pas besoin de préciser le type d'action car compris dans le noeud théorique
    // valeur qui va être en doublon car on a fusionné SPRB et action mais pas grave
    @Column(nullable = true)
    private final Long idNoeudTheorique;

    @Column(nullable = false)
    private final float stackEffectif;

    @Column(nullable = false)
    private final float pot;
    @Column(nullable = false)
    private final float potBounty;

    @OneToMany(mappedBy = "noeudSituation")
    private List<NoeudAction> noeudsActions;

    public NoeudSituation(FormatSolution formatSolution, ProfilJoueur profilJoueur, Long idNoeudTheorique,
                       float stackEffectif, float pot, float potBounty) {
        this.formatSolution = formatSolution;
        this.profilJoueur = profilJoueur;
        this.idNoeudTheorique = idNoeudTheorique;
        this.stackEffectif = stackEffectif;
        this.pot = pot;
        this.potBounty = potBounty;
        this.noeudsActions = new ArrayList<>();
    }

    public float getStackEffectif() {
        return stackEffectif;
    }

    public float getPot() {
        return pot;
    }

    public float getPotBounty() {
        return potBounty;
    }

    public List<NoeudAction> getNoeudsActions() {
        return noeudsActions;
    }
}
