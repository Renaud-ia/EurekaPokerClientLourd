package analyzor.modele.extraction.winamax;

import analyzor.modele.extraction.GestionnaireRoom;
import analyzor.modele.extraction.LecteurPartie;
import analyzor.modele.parties.PokerRoom;

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
    protected boolean ajouterFichier(Path cheminDuFichier) {
        if (super.ajouterFichier(cheminDuFichier)) return false;

        LecteurPartie lecteur = new LecteurWinamax(cheminDuFichier);
        if (!lecteur.fichierEstValide()) return false;
        Integer nombreMains = lecteur.sauvegarderPartie();

        if (nombreMains != null) {
            super.nombreMains += nombreMains;
            return true;
        }

        return false;

    }

    @Override
    protected boolean fichierEstValide(Path cheminDuFichier) {
        LecteurPartie lecteur = new LecteurWinamax(cheminDuFichier);
        return lecteur.fichierEstValide();
    }
}
