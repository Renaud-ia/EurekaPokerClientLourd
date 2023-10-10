package analyzor.modele.extraction;

import analyzor.modele.parties.PokerRoom;
import jakarta.persistence.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@Entity
public class FichierImport {
    /*
    on ne stocke que le nom du fichier (=String) pas tout le chemin
     */
    @Id
    private Integer id;

    @Enumerated(EnumType.STRING)
    private PokerRoom room;

    private String nomFichier;

    //constructeurs
    public FichierImport() {}

    public FichierImport(String nomFichier, PokerRoom room) {
        this.nomFichier = nomFichier;
        this.room = room;
        this.id = nomFichier.hashCode();
    }

    //getters, setters
    public String getNom() {
        return nomFichier;
    }
}
