package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Joueur {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ProfilJoueur profil;

    //constructeurs
    public Joueur() {

    }

    //getters, setters

    public Long getId() {
        return id;
    }

    // recommandé de réécrire equals et hashCode quand relation réciproque
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Joueur )) return false;
        return id != null && id.equals(((Joueur) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
