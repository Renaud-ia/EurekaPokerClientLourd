package analyzor.controleur;

import analyzor.modele.extraction.ControleGestionnaire;
import analyzor.modele.extraction.GestionnaireRoom;
import analyzor.modele.extraction.WorkerImportation;
import analyzor.modele.extraction.ipoker.GestionnaireIPoker;
import analyzor.modele.extraction.winamax.GestionnaireWinamax;
import analyzor.vue.donnees.InfosRoom;
import analyzor.vue.importmains.FenetreImport;
import analyzor.vue.FenetrePrincipale;

import javax.swing.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * controleur de l'import des mains
 */
public class ControleurRoom implements ControleurSecondaire {
    //todo : il faut ajouter les gestionnaires qu'on prend en charge ici
    private final ControleGestionnaire[] gestionnaires = {GestionnaireWinamax.obtenir(), GestionnaireIPoker.obtenir()};
    private final ControleurPrincipal controleurPrincipal;
    private final FenetreImport fenetreImport;
    private LinkedList<InfosRoom> listeInfosRoom;
    private WorkerImportation workerImport;


    ControleurRoom(FenetrePrincipale fenetrePrincipale, ControleurPrincipal controleurPrincipal) {
        this.controleurPrincipal = controleurPrincipal;
        this.fenetreImport = new FenetreImport(this, fenetrePrincipale);

        this.listeInfosRoom = new LinkedList<>();
    }


    @Override
    public void demarrer() {
        construireTableDonnees();
        fenetreImport.rafraichirDonnees();
        rafraichirWorker();
        lancerVue();
    }

    // méthodes privées de construction et d'actualisation de la table

    private void construireTableDonnees() {
        for(ControleGestionnaire gestionnaire : gestionnaires) {
            InfosRoom infosRoom = new InfosRoom(
                    gestionnaire.getNomRoom(),
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

    /**
     * appelé après le travail du worker pour actualiser le nombre de mains importés
     * ne touche pas aux dossiers
     */
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


    // méthodes publiques

    public void detection(InfosRoom infosRoom) {
        int indexRoom = selectionnerRoom(infosRoom);

        if (gestionnaires[indexRoom].autoDetection()) {
            infosRoom.setDossiers(gestionnaires[indexRoom].getDossiers());
            actualiserVues();
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

    /**
     * va créer le worker et transmet la progress bar à la fenêtre d'import
     */
    public void rafraichirWorker() {
        workerImport = new WorkerImportation("Import");
        for (ControleGestionnaire gestionnaireRoom : gestionnaires) {
            workerImport.ajouterLecteurs(gestionnaireRoom.importer());
        }
        fenetreImport.ajouterProgressBar(workerImport.getProgressBar());
    }

    public void lancerWorker() {
        workerImport.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName())) {
                if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                    if (workerImport.isCancelled()) {
                        fenetreImport.messageInfo("Import interrompu");
                    }
                    else fenetreImport.messageInfo("Import terminé");
                    rafraichirDonnees();
                    rafraichirWorker();
                    System.out.println("ON EST ICI");
                }
            }
        });

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
        fenetreImport.pack();
        fenetreImport.setVisible(true);
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

    public void afficherLogs(InfosRoom infosRoom) {
        //todo
    }
}
