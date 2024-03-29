package analyzor.modele.arbre.noeuds;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.ProfilJoueur;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


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
        return formatSolution.getId();
    }

    @Override
    public long getIdNoeudSituation() {
        return idNoeudTheorique;
    }
}
