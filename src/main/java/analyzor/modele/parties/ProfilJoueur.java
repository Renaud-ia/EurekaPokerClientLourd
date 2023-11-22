package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class ProfilJoueur {
    @Id
    private String nom;

    // suppression d'un profil n'affecte pas joueur
    @OneToMany(mappedBy = "profil")
    private Set<Joueur> joueurs = new HashSet<>();

    //constructeurs
    public ProfilJoueur(String nom) {
        this.nom = nom;
    }

    // pour Hibernate
    public ProfilJoueur() {}

    public String getNom() {
        return nom;
    }

    public Set<Joueur> getJoueurs() {
        return joueurs;
    }

    //getters, setters
}
