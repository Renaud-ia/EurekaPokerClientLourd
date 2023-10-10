package analyzor.modele.extraction;

import analyzor.modele.parties.PokerRoom;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Entity
public class DossierImport {
    @Id
    private Integer id;

    @Enumerated(EnumType.STRING)
    private PokerRoom room;

    private String cheminDossier;

    @Column(nullable = true)
    Integer nFichiersImportes;

    //suppression du dossier => on passe à non actif mais on garde la référence
    @Column(nullable = true)
    Boolean actif;

    //constructeurs
    public DossierImport() {}
    public DossierImport(PokerRoom room, Path cheminDossier) {
        this.room = room;
        this.cheminDossier = cheminDossier.toString();
        this.id = cheminDossier.hashCode();
    }

    public void desactiver() {
        this.actif = false;
    }

    //getters setters
    public Path getChemin() {
        return Paths.get(cheminDossier);
    }

    public int getnFichiersImportes() {
        if (nFichiersImportes == null) return 0;
        return nFichiersImportes;
    }

    public void setnFichiersImportes(int nFichiersImportes) {
        this.nFichiersImportes = nFichiersImportes;
    }

    public void fichierAjoute() {
        if (this.nFichiersImportes == null) nFichiersImportes = 1;
        else this.nFichiersImportes += 1;
    }

    public void fichiersAjoutes(int fichiersReconnus) {
        if (this.nFichiersImportes == null) nFichiersImportes = fichiersReconnus;
        else this.nFichiersImportes += fichiersReconnus;
    }
}
