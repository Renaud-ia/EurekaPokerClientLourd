package analyzor.modele.poker;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.parties.ProfilJoueur;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class RangeSauvegardable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private ProfilJoueur profil;
    @ManyToOne
    private NoeudAction noeudArbre;

    public void setProfil(ProfilJoueur profil) {
        this.profil = profil;
    }

    public void setNoeudAction(NoeudAction noeudAction) {
        this.noeudArbre = noeudAction;
    }
}
