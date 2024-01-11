package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Id sur la base de String nom (max 12 caractères Winamax)
 * aucune collision possible dans la BDD
 * collision gérée correctement par equals() dans HashMap
 */
@Entity
public class Joueur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nom;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<ProfilJoueur> profils;

    //constructeurs
    public Joueur() {}

    public Joueur(String nom) {
        this.nom = nom;
    }

    // recommandé de réécrire equals et hashCode quand relation réciproque
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Joueur)) return false;
        return nom.equals(((Joueur) o).getNom());
    }

    @Override
    public int hashCode() {
        return nom.hashCode();
    }

    public String getNom() {
        return nom;
    }

    public void addProfil(ProfilJoueur profilJoueur) {
        this.profils.add(profilJoueur);
    }

    @Override
    public String toString() {
        return "JOUEUR : " + nom;
    }
}
