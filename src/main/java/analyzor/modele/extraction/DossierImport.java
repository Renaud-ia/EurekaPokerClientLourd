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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private PokerRoom room;

    private String cheminDossier;

    
    @Column(nullable = false)
    Boolean actif;

    
    public DossierImport() {}
    public DossierImport(PokerRoom room, Path cheminDossier) {
        this.room = room;
        this.cheminDossier = cheminDossier.toString();
        this.actif = true;
    }

    public void desactiver() {
        this.actif = false;
    }

    
    public Path getChemin() {
        return Paths.get(cheminDossier);
    }

    public boolean estActif() {
        return actif;
    }
}
