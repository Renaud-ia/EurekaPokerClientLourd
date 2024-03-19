package analyzor.modele.extraction.ipoker;

import analyzor.modele.extraction.GestionnaireRoom;
import analyzor.modele.extraction.LecteurPartie;
import analyzor.modele.extraction.winamax.GestionnaireWinamax;
import analyzor.modele.extraction.winamax.LecteurWinamax;
import analyzor.modele.parties.PokerRoom;
import analyzor.vue.basiques.Images;

import javax.swing.*;
import java.io.File;
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

    protected void ajouterDossiersRecherche() {
        String userHome = System.getProperty("user.home");

        // dossiers des trackers
        dossiersDetection.add("C:\\Program Files (x86)\\Xeester\\processed\\iPoker Network");
        String dossierPT4 = userHome + "\\AppData\\Local\\PokerTracker 4\\Processed\\iPoker Network";
        dossiersDetection.add(dossierPT4);

        // dossiers de Betclic
        dossiersDetection.add("C:\\Program Files (x86)\\Betclic Poker.fr\\data");
        dossiersDetection.add("C:\\Poker\\BetclicPoker.fr\\History");
        dossiersDetection.add(
                System.getProperty("user.home") +
                        "AppData\\Local\\VirtualStore\\Program Files (x86)\\Betclic Poker.fr");

        String dossierBase = userHome + "\\AppData\\Local\\Betclic Poker.fr\\data";
        dossiersDetection.addAll(trouverDossiersHistoriquesParUser(dossierBase, "History"));
    }

    //pattern Singleton
    public static GestionnaireIPoker obtenir() {
        if (instance == null) {
            instance = new GestionnaireIPoker();
        }
        return instance;
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
