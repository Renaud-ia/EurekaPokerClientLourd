package analyzor.modele.extraction;

import javax.swing.*;
import java.util.List;

public interface ControleGestionnaire {
    public void actualiserDonnees();
    boolean autoDetection();
    List<LecteurPartie> importer();
    boolean ajouterDossier(String cheminDuDossier);
    boolean supprimerDossier(String cheminDuDossier);
    
    
    String getNomRoom();
    boolean getConfiguration();
    List<String> getDossiers();
    int getNombreFichiersImportes();
    int getNombreMainsImportees();
    int getNombreErreursImport();
    List<FichierImport> getPartiesNonImportees();
    void supprimerImportsRates();
    ImageIcon getIcone();
}

