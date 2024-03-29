package analyzor.vue.donnees.rooms;

import analyzor.modele.extraction.DossierImport;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InfosRoom {
    private final String nom;
    private final ImageIcon iconeRoom;
    private boolean active;
    private List<String> dossiers;
    private boolean etat;
    private int nFichiersImportes;
    private int nMainsImportees;
    private int nErreurs;

    public InfosRoom(String nomRoom,
                     ImageIcon icone,
                     boolean configuration,
                     List<String> dossiers,
                     int nombreFichiersImportes,
                     int nombreMainsImportees,
                     int nombreErreursImport) {
        this.nom = nomRoom;
        this.iconeRoom = icone;
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

    public ImageIcon getIcone() {
        return iconeRoom;
    }
}

