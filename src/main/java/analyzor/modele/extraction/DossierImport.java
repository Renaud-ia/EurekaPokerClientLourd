package analyzor.modele.extraction;

import analyzor.modele.parties.PokerRoom;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import jakarta.persistence.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Entity
public class DossierImport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PokerRoom room;

    private String cheminDossier;

    int nFichiersImportes;

    //suppression du dossier => on passe à non actif mais on garde la référence
    boolean actif;

    //constructeurs
    public DossierImport() {}
    public DossierImport(PokerRoom room, Path cheminDossier) {
        this.room = room;
        this.cheminDossier = cheminDossier.toString();
        this.actif = true;
    }

    public void desactiver() {
        this.actif = false;
    }

    //getters setters
    public Path getChemin() {
        return Paths.get(cheminDossier);
    }

    public int getnFichiersImportes() {
        return nFichiersImportes;
    }

    public void setnFichiersImportes(int nFichiersImportes) {
        this.nFichiersImportes = nFichiersImportes;
    }
}
