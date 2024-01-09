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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private PokerRoom room;

    private String nomFichier;
    @Enumerated(EnumType.STRING)
    private StatutImport statutImportation;

    //constructeurs
    public FichierImport() {}

    public FichierImport(PokerRoom room, String nomFichier) {
        this.room = room;
        this.nomFichier = nomFichier;
    }

    //getters, setters
    public String getNom() {
        return nomFichier;
    }

    public void setStatut(StatutImport statutImport) {
        this.statutImportation = statutImport;
    }

    public enum StatutImport {
        REUSSI, FICHIER_MANQUANT, FICHIER_CORROMPU, INFORMATIONS_INCORRECTES, PROBLEME_BDD, AUTRE
    }
}
