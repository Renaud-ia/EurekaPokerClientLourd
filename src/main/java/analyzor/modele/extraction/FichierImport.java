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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PokerRoom room;

    private String nomFichier;

    //constructeurs
    public FichierImport() {}

    public FichierImport(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    //getters, setters
    public String getNom() {
        return nomFichier;
    }
}
