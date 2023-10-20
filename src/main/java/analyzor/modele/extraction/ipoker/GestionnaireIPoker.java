package analyzor.modele.extraction.ipoker;

import analyzor.modele.extraction.GestionnaireRoom;
import analyzor.modele.extraction.LecteurPartie;
import analyzor.modele.extraction.winamax.GestionnaireWinamax;
import analyzor.modele.extraction.winamax.LecteurWinamax;
import analyzor.modele.parties.PokerRoom;

import java.nio.file.Path;

public class GestionnaireIPoker extends GestionnaireRoom {
    private static GestionnaireIPoker instance;
    private static final PokerRoom room = PokerRoom.IPOKER;

    private GestionnaireIPoker() {
        super(GestionnaireIPoker.room);
    }

    //pattern Singleton
    public static GestionnaireIPoker obtenir() {
        if (instance == null) {
            instance = new GestionnaireIPoker();
        }
        return instance;
    }

    @Override
    public boolean autoDetection() {
        //TODO
        System.out.println("Autodétection");
        // vérifie les emplacements classiques
        return false;
    }

    @Override
    protected Integer ajouterFichier(Path cheminDuFichier) {
        if (cheminsFichiers.contains(cheminDuFichier.getFileName().toString())) {
            logger.info("Fichier déjà présent dans cheminsFichier");
            return null;
        }

        LecteurPartie lecteur = new LecteurIPoker(cheminDuFichier);
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
        LecteurPartie lecteur = new LecteurIPoker(cheminDuFichier);
        return lecteur.fichierEstValide();
    }
}
