package analyzor.modele.extraction.winamax;

import analyzor.modele.extraction.GestionnaireRoom;
import analyzor.modele.extraction.LecteurPartie;
import analyzor.modele.parties.DataRoom;
import analyzor.modele.parties.PokerRoom;
import analyzor.modele.parties.RequetesBDD;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.nio.file.Path;

public class GestionnaireWinamax extends GestionnaireRoom {
    private static GestionnaireWinamax instance;
    private static final PokerRoom room = PokerRoom.WINAMAX;

    private GestionnaireWinamax() {
        super(GestionnaireWinamax.room);
    }

    //pattern Singleton
    public static GestionnaireWinamax obtenir() {
        if (instance == null) {
            instance = new GestionnaireWinamax();
        }
        return instance;
    }

    @Override
    public boolean autoDetection() {
        System.out.println("Autodétection");
        // vérifie les emplacements classiques
        // on va exclure les fichiers avec summary
        return false;
    }

    @Override
    protected Integer ajouterFichier(Path cheminDuFichier) {
        if (!cheminsFichiers.contains(cheminDuFichier.getFileName().toString())) return null;

        LecteurPartie lecteur = new LecteurWinamax(cheminDuFichier);
        if (!lecteur.fichierEstValide()) return null;
        Integer mainsAjoutees = lecteur.sauvegarderPartie();

        if (mainsAjoutees != null) {
            super.nombreMains += mainsAjoutees;

            return mainsAjoutees;
        }

        return null;

    }

    @Override
    protected boolean fichierEstValide(Path cheminDuFichier) {
        LecteurPartie lecteur = new LecteurWinamax(cheminDuFichier);
        return lecteur.fichierEstValide();
    }
}
