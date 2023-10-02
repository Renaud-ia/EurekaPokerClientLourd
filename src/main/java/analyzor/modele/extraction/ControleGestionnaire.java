package analyzor.modele.extraction;

import analyzor.controleur.ProgressionTache;
import analyzor.controleur.WorkerAffichable;

import java.nio.file.Path;

public interface ControleGestionnaire {
    boolean autoDetection();
    WorkerAffichable importer();
    boolean ajouterDossier(Path cheminDuDossier);
    boolean supprimerDossier(String cheminDuDossier);
    boolean ajouterFichier(Path cheminDuFichier);
    String getNomRoom();
    String getDetailRoom();
    boolean getConfiguration();
    int nombreDossiers();
    int nombreFichiers();
    int nombreMains();
    String[] getDossiers();
    int fichiersParDossier(String nomDossier);
}

