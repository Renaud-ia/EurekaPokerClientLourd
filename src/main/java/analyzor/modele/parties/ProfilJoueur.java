package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class ProfilJoueur {
    public static String nomProfilHero = "hero";
    public static String nomProfilVillain = "villain";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nomProfil;

    //todo tester si vraiment plus long lors de l'import d'ajouter la relation réciproque manyToMany avec Joueur en EAGER
    // car ça sera surement plus pratique

    //constructeurs
    public ProfilJoueur(String nomProfil) {
        this.nomProfil = nomProfil;
    }

    // pour Hibernate
    public ProfilJoueur() {}

    public String getNom() {
        return nomProfil;
    }

    public boolean isHero() {
        return Objects.equals(nomProfil, nomProfilHero);
    }

    //getters, setters
}
