package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class ProfilJoueur {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // suppression d'un profil n'affecte pas joueur
    @OneToMany(mappedBy = "profil")
    private List<Joueur> joueurs = new ArrayList<>();

    //constructeurs
    public ProfilJoueur() {}

    //getters, setters
}
