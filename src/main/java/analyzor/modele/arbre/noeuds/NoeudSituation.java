package analyzor.modele.arbre.noeuds;

import analyzor.modele.clustering.objets.MinMaxCalcul;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.simulation.SituationStackPotBounty;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * on a besoin d'un noeud Situation qui encapsule les NoeudsAction
 * d'une part évite de stocker deux fois la même info
 * d'autre part, indispensable pour Simulation car les actions possibles vont dépendre du NoeudSituation
 * les valeurs qui sont en mémoire (SPB) sont des valeurs normalisées !!!
 */
@Entity
public class NoeudSituation implements NoeudMesurable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ProfilJoueur profilJoueur;

    @ManyToOne
    @JoinColumn(nullable = false)
    private FormatSolution formatSolution;
    // pas besoin de préciser le type d'action car compris dans le noeud théorique
    // valeur qui va être en doublon car on a fusionné SPRB et action mais pas grave
    @Column(nullable = false)
    private Long idNoeudTheorique;

    @Column(nullable = false)
    private long codeStackEffectif;

    @Column(nullable = false)
    private float pot;
    @Column(nullable = false)
    private float potBounty;

    @OneToMany(mappedBy = "noeudSituation", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<NoeudAction> noeudsActions;

    public NoeudSituation() {}

    public NoeudSituation(FormatSolution formatSolution, ProfilJoueur profilJoueur, Long idNoeudTheorique,
                          long stackEffectif, float pot, float potBounty) {
        this.formatSolution = formatSolution;
        this.profilJoueur = profilJoueur;
        this.idNoeudTheorique = idNoeudTheorique;
        this.codeStackEffectif = stackEffectif;
        this.pot = pot;
        this.potBounty = potBounty;
        this.noeudsActions = new ArrayList<>();
    }


    @Override
    public float getPot() {
        return pot;
    }

    @Override
    public float getPotBounty() {
        return potBounty;
    }

    public List<NoeudAction> getNoeudsActions() {
        return noeudsActions;
    }

    @Override
    public long getCodeStackEffectif() {
        return codeStackEffectif;
    }

    @Override
    public long getIdFormatSolution() {
        return idNoeudTheorique;
    }

    @Override
    public long getIdNoeudSituation() {
        return formatSolution.getId();
    }
}
