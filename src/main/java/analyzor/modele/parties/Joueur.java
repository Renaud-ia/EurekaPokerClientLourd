package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Joueur {
    @Id
    private Integer id;

    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProfilJoueur profil;

    @OneToMany(fetch = FetchType.LAZY)
    private List<GainSansAction> gainSansActions = new ArrayList<>();

    //constructeurs
    public Joueur() {}

    public Joueur(String nom) {
        this.id = nom.hashCode();
        this.nom = nom;
    }

    //getters, setters

    public int getId() {
        return id;
    }

    public List<GainSansAction> getGainSansActions() {
        return gainSansActions;
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
        return Objects.hash(nom);
    }

}
