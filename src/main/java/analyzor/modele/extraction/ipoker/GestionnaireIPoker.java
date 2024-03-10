package analyzor.modele.extraction.ipoker;

import analyzor.modele.extraction.GestionnaireRoom;
import analyzor.modele.extraction.LecteurPartie;
import analyzor.modele.extraction.winamax.GestionnaireWinamax;
import analyzor.modele.extraction.winamax.LecteurWinamax;
import analyzor.modele.parties.PokerRoom;
import analyzor.vue.basiques.Images;

import javax.swing.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GestionnaireIPoker extends GestionnaireRoom {
    private static GestionnaireIPoker instance;
    private static final PokerRoom room = PokerRoom.IPOKER;

    private GestionnaireIPoker() {
        super(GestionnaireIPoker.room);
        icone = new ImageIcon(Images.logoBetclic);
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
    public List<LecteurPartie> importer() {
        // va importer tous les fichiers des dossiers qui existent
        List<LecteurPartie> lecteurImports = new ArrayList<>();

        // on construit d'abord la liste des fichiers à importer
        List<Path> nouveauxFichiers = listerNouveauxFichiers();
        for (Path fichier : nouveauxFichiers) {
            lecteurImports.add(new LecteurIPoker(fichier));
        }

        return lecteurImports;
    }

    @Override
    protected boolean fichierEstValide(Path cheminDuFichier) {
        LecteurPartie lecteur = new LecteurIPoker(cheminDuFichier);
        return lecteur.fichierEstValide();
    }
}
