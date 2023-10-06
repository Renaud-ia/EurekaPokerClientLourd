package analyzor.modele.extraction;

import analyzor.controleur.ProgressionTache;
import analyzor.controleur.WorkerAffichable;

import java.nio.file.Path;
import java.util.List;

public interface ControleGestionnaire {
    boolean autoDetection();
    WorkerAffichable importer();
    boolean ajouterDossier(Path cheminDuDossier);
    boolean supprimerDossier(String cheminDuDossier);
    //todo à intégrer?
    /*
    ajout de fichier individuel désactivé
    boolean ajouterFichier(Path cheminDuFichier);
     */
    String getNomRoom();
    boolean getConfiguration();
    int nombreDossiers();
    int nombreFichiers();
    int nombreMains();
    List<DossierImport> getDossiers();
}

