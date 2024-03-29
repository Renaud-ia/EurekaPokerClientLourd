package analyzor.controleur;

import analyzor.modele.extraction.ControleGestionnaire;
import analyzor.modele.extraction.FichierImport;
import analyzor.modele.extraction.WorkerImportation;
import analyzor.modele.extraction.ipoker.GestionnaireIPoker;
import analyzor.modele.extraction.winamax.GestionnaireWinamax;
import analyzor.vue.donnees.rooms.DTOPartieVisible;
import analyzor.vue.donnees.rooms.InfosRoom;
import analyzor.vue.importmains.FenetreImport;
import analyzor.vue.FenetrePrincipale;
import analyzor.vue.importmains.LogsRoom;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ControleurRoom implements ControleurSecondaire {

    private final ControleGestionnaire[] gestionnaires = {GestionnaireIPoker.obtenir(), GestionnaireWinamax.obtenir()};
    private final ControleurPrincipal controleurPrincipal;
    private final FenetreImport fenetreImport;
    private final LogsRoom vueLogsRooms;
    private LinkedList<InfosRoom> listeInfosRoom;
    private WorkerImportation workerImport;
    private boolean workerEnCours;


    ControleurRoom(FenetrePrincipale fenetrePrincipale, ControleurPrincipal controleurPrincipal) {
        this.controleurPrincipal = controleurPrincipal;
        this.fenetreImport = new FenetreImport(this, fenetrePrincipale);
        this.vueLogsRooms = new LogsRoom(this, fenetreImport);

        this.listeInfosRoom = new LinkedList<>();
        workerEnCours = false;
    }

    @Override
    public void demarrer() {
        construireTableDonnees();
        fenetreImport.rafraichirDonnees();
        rafraichirWorker();
    }



    private void construireTableDonnees() {
        for(ControleGestionnaire gestionnaire : gestionnaires) {
            InfosRoom infosRoom = new InfosRoom(
                    gestionnaire.getNomRoom(),
                    gestionnaire.getIcone(),
                    gestionnaire.getConfiguration(),
                    gestionnaire.getDossiers(),
                    gestionnaire.getNombreFichiersImportes(),
                    gestionnaire.getNombreMainsImportees(),
                    gestionnaire.getNombreErreursImport()
            );

            fenetreImport.ajouterRoom(infosRoom);
            listeInfosRoom.add(infosRoom);
        }
    }

    
    private void rafraichirDonnees() {
        int index = 0;
        for (ControleGestionnaire gestionnaire : gestionnaires) {
            gestionnaire.actualiserDonnees();
            InfosRoom infosRoom = listeInfosRoom.get(index++);
            if (infosRoom == null) {
                throw new RuntimeException("Vue room non trouvée");
            }

            infosRoom.actualiserValeurs(
                    gestionnaire.getNombreFichiersImportes(),
                    gestionnaire.getNombreMainsImportees(),
                    gestionnaire.getNombreErreursImport()
                    );
        }

        fenetreImport.rafraichirDonnees();
    }




    public void detection(InfosRoom infosRoom) {
        int indexRoom = selectionnerRoom(infosRoom);

        if (gestionnaires[indexRoom].autoDetection()) {
            infosRoom.setDossiers(gestionnaires[indexRoom].getDossiers());
            actualiserVues();
            rafraichirWorker();
        }
        else {
            fenetreImport.messageInfo("Aucun nouveau dossier trouv\u00E9");
        }
    }

    public void ajouterDossier(InfosRoom infosRoom, String nomDossier) {
        int indexRoom = selectionnerRoom(infosRoom);
        if (gestionnaires[indexRoom].ajouterDossier(nomDossier)) {
            infosRoom.setDossiers(gestionnaires[indexRoom].getDossiers());
            actualiserVues();
            rafraichirWorker();
            fenetreImport.messageInfo("Dossier ajout\u00E9 avec succ\u00E8s");
        }
        else {
            fenetreImport.messageErreur("Le dossier n'a pas pu \u00EAtre ajout\u00E9");
        }

    }

    public void supprimerDossier(InfosRoom infosRoom, String nomDossier) {
        int indexRoom = selectionnerRoom(infosRoom);

        if (gestionnaires[indexRoom].supprimerDossier(nomDossier)) {
            infosRoom.supprimerDossier(nomDossier);
            actualiserVues();
            fenetreImport.messageInfo("Dossier supprim\u00E9");
        }
        else {
            fenetreImport.messageErreur("Le dossier n'a pas pu \u00EAtre supprim\u00E9");
        }
    }

    
    public void afficherLogs(InfosRoom infosRoom) {
        int indexRoom = selectionnerRoom(infosRoom);
        vueLogsRooms.reset();

        vueLogsRooms.setNomRoom(infosRoom);

        List<FichierImport> listeMainsNonImportees = gestionnaires[indexRoom].getPartiesNonImportees();
        List<DTOPartieVisible> partiesNonImportees = new ArrayList<>();

        for (FichierImport fichierImport : listeMainsNonImportees) {
            DTOPartieVisible partieVisible =
                    new DTOPartieVisible(fichierImport.getCheminFichier(), fichierImport.getStatutImport());
            partiesNonImportees.add(partieVisible);
        }

        vueLogsRooms.setMainsNonImportees(partiesNonImportees);
        vueLogsRooms.afficher();
    }

    
    public void retenterImport(InfosRoom infosRoom) {
        int indexRoom = selectionnerRoom(infosRoom);
        gestionnaires[indexRoom].supprimerImportsRates();
        infosRoom.resetFichiersNonImportes();
        fenetreImport.rafraichirDonnees();
        rafraichirWorker();
        vueLogsRooms.setVisible(false);
        fenetreImport.setVisible(true);
    }

    
    public void rafraichirWorker() {
        Thread rafraichissementWorker = new Thread(() -> {
            if (workerImport != null) workerImport.cancel(true);
            fenetreImport.desactiverBoutons();
            workerImport = new WorkerImportation("Import");
            for (ControleGestionnaire gestionnaireRoom : gestionnaires) {
                workerImport.ajouterLecteurs(gestionnaireRoom.importer());
            }
            fenetreImport.ajouterProgressBar(workerImport.getProgressBar());
            fenetreImport.reactiverBoutons();
            fenetreImport.setBoutonCalcul(workerImport.calculPossible());
        });

        rafraichissementWorker.start();
    }

    public void lancerWorker() {
        if (workerEnCours) return;
        workerImport.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName())) {
                if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                    if (workerImport.isCancelled()) {
                        fenetreImport.messageInfo("Import interrompu");
                    }
                    else fenetreImport.messageInfo("Import termin\u00E9");
                    fenetreImport.reactiverControles();
                    rafraichirDonnees();
                    rafraichirWorker();
                    workerEnCours = false;
                }
            }
        });

        workerEnCours = true;

        workerImport.execute();
    }

    public void arreterWorker() {
        workerImport.cancel(false);
    }

    private void actualiserVues() {
        fenetreImport.rafraichirDonnees();
    }

    @Override
    public void lancerVue() {
        actualiserVues();
        fenetreImport.afficher();
    }

    @Override
    public void desactiverVue() {
        fenetreImport.setVisible(false);
    }

    private int selectionnerRoom(InfosRoom infosRoom) {
        int indexRoom = listeInfosRoom.indexOf(infosRoom);
        if (indexRoom == -1) throw new IllegalArgumentException("Room non trouvée");

        return indexRoom;
    }


}
