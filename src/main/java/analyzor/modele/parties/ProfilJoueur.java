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

    
    

    
    public ProfilJoueur(String nomProfil) {
        this.nomProfil = nomProfil;
    }

    
    public ProfilJoueur() {}

    public String getNom() {
        return nomProfil;
    }

    public boolean isHero() {
        return Objects.equals(nomProfil, nomProfilHero);
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfilJoueur)) return false;
        return id == ((ProfilJoueur) o).id;
    }

    @Override
    
    public int hashCode() {
        return (int) id;
    }

    public String toString() {
        return nomProfil;
    }
}
