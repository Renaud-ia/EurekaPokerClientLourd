package analyzor.modele.extraction;

import analyzor.modele.parties.PokerRoom;
import jakarta.persistence.*;

/**
 * classe de stockage des fichiers importés
 * permet de garder aussi les fichiers dont l'import a raté + le statut
 */
@Entity
public class FichierImport {

    public enum StatutImport {
        REUSSI, FICHIER_MANQUANT, FICHIER_CORROMPU, INFORMATIONS_INCORRECTES, PROBLEME_BDD, AUTRE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private PokerRoom room;

    private String nomFichier;
    private String cheminComplet;
    private int nombreDeMainsImportees;
    @Enumerated(EnumType.STRING)
    private StatutImport statutImportation;

    //constructeurs
    public FichierImport() {}

    public FichierImport(PokerRoom room, String nomFichier) {
        this.room = room;
        this.nomFichier = nomFichier;
    }

    //getters, setters
    public String getNomFichier() {
        return nomFichier;
    }

    public void setStatut(StatutImport statutImport) {
        this.statutImportation = statutImport;
    }

    public void setCheminComplet(String cheminComplet) {
        this.cheminComplet = cheminComplet;
    }

    public void setNombreMainsImportees(Integer nombreDeMainsImportees) {
        this.nombreDeMainsImportees = nombreDeMainsImportees;
    }

    public int getNombreMains() {
        return nombreDeMainsImportees;
    }

    public boolean estReussi() {
        return statutImportation == StatutImport.REUSSI;
    }

    public String getCheminFichier() {
        return cheminComplet;
    }

    public String getStatutImport() {
        return statutImportation.toString();
    }
}
