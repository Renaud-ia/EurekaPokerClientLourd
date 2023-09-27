package analyzor.modele.extraction;

import java.nio.file.Path;

public class GestionnaireWinamax extends GestionnaireRoom {
    private static GestionnaireWinamax instance;
    private static final String nomRoom = "Winamax";
    private static final String detailRoom = "";

    private GestionnaireWinamax() {
        super(GestionnaireWinamax.nomRoom, GestionnaireWinamax.detailRoom);

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
        // vérifie les emplacements classiques
        return false;
    }

    @Override
    public boolean ajouterDossier(Path cheminDuDossier) {
        return false;
    }

    @Override
    public boolean ajouterFichier(Path cheminDuFichier) {
        LecteurPartie lecteur = new LecteurWinamax(cheminDuFichier);
        if (!lecteur.fichierEstValide()) return false;
        Integer nombreMains = lecteur.sauvegarderPartie();

        if (nombreMains != null) {
            super.nombreMains += nombreMains;
            return true;
        }

        return false;

    }
}
