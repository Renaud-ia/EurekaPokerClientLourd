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

    // todo est ce la bonne manière de faire pour créer un set de Profil dans un joueur ?
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfilJoueur)) return false;
        return id == ((ProfilJoueur) o).id;
    }

    @Override
    // todo : attention si on en met énormément ça peut buguer car cast long vers ints
    public int hashCode() {
        return (int) id;
    }
}
