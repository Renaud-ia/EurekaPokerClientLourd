package analyzor.vue.donnees.rooms;

import analyzor.modele.extraction.DossierImport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InfosRoom {
    private final String nom;
    private boolean active;
    private List<String> dossiers;
    private boolean etat;
    private int nFichiersImportes;
    private int nMainsImportees;
    private int nErreurs;

    public InfosRoom(String nomRoom,
                     boolean configuration,
                     List<String> dossiers,
                     int nombreFichiersImportes,
                     int nombreMainsImportees,
                     int nombreErreursImport) {
        this.nom = nomRoom;
        this.active = configuration;
        this.dossiers = dossiers;
        this.nFichiersImportes = nombreFichiersImportes;
        this.nMainsImportees = nombreMainsImportees;
        this.nErreurs = nombreErreursImport;
    }

    public List<String> getDossiers() {
        return dossiers;
    }

    public String getNombreFichiersImportes() {
        return Integer.toString(nFichiersImportes);
    }

    public String getNombreMainsImportees() {
        return Integer.toString(nMainsImportees);
    }

    public String getNombreErreursImport() {
        return Integer.toString(nErreurs);
    }

    /**
     * actualise la liste des dossiers
     * @param dossiersAjoutes : la liste de tous les dossiers récupérés depuis le modèle
     */
    public void setDossiers(List<String> dossiersAjoutes) {
        for (String nouveauDossier : dossiersAjoutes) {
            if (!dossiers.contains(nouveauDossier)) {
                dossiers.add(nouveauDossier);
            }
        }
    }

    public void actualiserValeurs(int nombreFichiersImportes, int nombreMainsImportees, int nombreErreursImport) {
        this.nFichiersImportes = nombreFichiersImportes;
        this.nMainsImportees = nombreMainsImportees;
        this.nErreurs = nombreErreursImport;
    }

    public String getNom() {
        return nom;
    }

    public void supprimerDossier(String nomDossier) {
        this.dossiers.remove(nomDossier);
    }

    public void resetFichiersNonImportes() {
        this.nErreurs = 0;
    }
}

