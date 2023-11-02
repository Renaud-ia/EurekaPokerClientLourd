package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ProfilJoueur {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nom;

    // suppression d'un profil n'affecte pas joueur
    @OneToMany(mappedBy = "profil")
    private List<Joueur> joueurs = new ArrayList<>();

    //constructeurs
    public ProfilJoueur(String nom) {
        this.nom = nom;
    }

    // pour Hibernate
    public ProfilJoueur() {}

    //getters, setters
}
