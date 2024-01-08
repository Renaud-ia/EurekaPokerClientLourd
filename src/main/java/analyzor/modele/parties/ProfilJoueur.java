package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class ProfilJoueur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nomProfil;
    private boolean hero;

    // suppression d'un profil n'affecte pas joueur
    @OneToMany(mappedBy = "profil", fetch = FetchType.EAGER)
    private Set<Joueur> joueurs = new HashSet<>();

    //constructeurs
    public ProfilJoueur(String nomProfil, boolean hero) {
        this.nomProfil = nomProfil;
        this.hero = hero;
    }

    // pour Hibernate
    public ProfilJoueur() {}

    public String getNom() {
        return nomProfil;
    }

    public Set<Joueur> getJoueurs() {
        return joueurs;
    }

    public void changerNom(String jajfaf) {
        this.nomProfil = jajfaf;
    }

    //getters, setters
}
