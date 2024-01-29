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

    //suppression du dossier => on passe à non actif mais on garde la référence
    @Column(nullable = false)
    Boolean actif;

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

    public boolean estActif() {
        return actif;
    }
}
