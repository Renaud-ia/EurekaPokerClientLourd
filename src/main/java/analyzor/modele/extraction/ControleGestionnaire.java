package analyzor.modele.extraction;

import analyzor.controleur.WorkerAffichable;

import java.nio.file.Path;
import java.util.List;

public interface ControleGestionnaire {
    public void actualiserDonnees();
    boolean autoDetection();
    List<LecteurPartie> importer();
    boolean ajouterDossier(String cheminDuDossier);
    boolean supprimerDossier(String cheminDuDossier);
    //todo à intégrer?
    /*
    ajout de fichier individuel désactivé
    boolean ajouterFichier(Path cheminDuFichier);
     */
    String getNomRoom();
    boolean getConfiguration();
    List<String> getDossiers();
    int getNombreFichiersImportes();
    int getNombreMainsImportees();
    int getNombreErreursImport();
    List<FichierImport> getPartiesNonImportees();
    void supprimerImportsRates();
}

