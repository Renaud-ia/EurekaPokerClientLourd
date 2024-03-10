package analyzor.modele.extraction.winamax;

import analyzor.modele.extraction.GestionnaireRoom;
import analyzor.modele.extraction.LecteurPartie;
import analyzor.modele.extraction.ipoker.LecteurIPoker;
import analyzor.modele.parties.PokerRoom;
import analyzor.vue.basiques.Images;

import javax.swing.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GestionnaireWinamax extends GestionnaireRoom {
    private static GestionnaireWinamax instance;
    private static final PokerRoom room = PokerRoom.WINAMAX;

    private GestionnaireWinamax() {
        super(GestionnaireWinamax.room);
        icone = new ImageIcon(Images.logoWinamax);
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
        //TODO
        System.out.println("Autodétection");
        // vérifie les emplacements classiques
        // on va exclure les fichiers avec summary
        return false;
    }

    @Override
    public List<LecteurPartie> importer() {
        // va importer tous les fichiers des dossiers qui existent
        List<LecteurPartie> lecteurImports = new ArrayList<>();

        // on construit d'abord la liste des fichiers à importer
        List<Path> nouveauxFichiers = listerNouveauxFichiers();
        for (Path fichier : nouveauxFichiers) {
            lecteurImports.add(new LecteurWinamax(fichier));
        }

        return lecteurImports;
    }

    @Override
    protected boolean fichierEstValide(Path cheminDuFichier) {
        LecteurPartie lecteur = new LecteurWinamax(cheminDuFichier);
        return lecteur.fichierEstValide();
    }
}
