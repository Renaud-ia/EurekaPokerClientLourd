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


    public void changerNom(String jajfaf) {
        this.nomProfil = jajfaf;
    }

    //getters, setters
}
