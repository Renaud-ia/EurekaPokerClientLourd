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

/**
 * controleur de l'import des mains
 */
public class ControleurRoom implements ControleurSecondaire {
    //todo : il faut ajouter les gestionnaires qu'on prend en charge ici
    private final ControleGestionnaire[] gestionnaires = {GestionnaireIPoker.obtenir(), GestionnaireWinamax.obtenir()};
    private final ControleurPrincipal controleurPrincipal;
    private final FenetreImport fenetreImport;
    private final LogsRoom vueLogsRooms;
    private LinkedList<InfosRoom> listeInfosRoom;
    private WorkerImportation workerImport;


    ControleurRoom(FenetrePrincipale fenetrePrincipale, ControleurPrincipal controleurPrincipal) {
        this.controleurPrincipal = controleurPrincipal;
        this.fenetreImport = new FenetreImport(this, fenetrePrincipale);
        this.vueLogsRooms = new LogsRoom(this, fenetreImport);

        this.listeInfosRoom = new LinkedList<>();
    }




    @Override
    public void demarrer() {
        construireTableDonnees();
        fenetreImport.rafraichirDonnees();
        rafraichirWorker();
    }

    // méthodes privées de construction et d'actualisation de la table

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

    /**
     * affichage des logs de mains non importés pour une room donnée
     * @param infosRoom la room qui a appelé le schmiliblick
     */
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

    /**
     * méthode pour retenter l'import des fichiers rates
     * on va juste les ajouter au worker
     * @param infosRoom
     */
    public void retenterImport(InfosRoom infosRoom) {
        int indexRoom = selectionnerRoom(infosRoom);
        gestionnaires[indexRoom].supprimerImportsRates();
        infosRoom.resetFichiersNonImportes();
        fenetreImport.rafraichirDonnees();
        rafraichirWorker();
        vueLogsRooms.setVisible(false);
    }

    /**
     * va créer le worker et transmet la progress bar à la fenêtre d'import
     */
    public void rafraichirWorker() {
        Thread rafraichissementWorker = new Thread(() -> {
            fenetreImport.desactiverBoutons();
            workerImport = new WorkerImportation("Import");
            for (ControleGestionnaire gestionnaireRoom : gestionnaires) {
                workerImport.ajouterLecteurs(gestionnaireRoom.importer());
            }
            fenetreImport.ajouterProgressBar(workerImport.getProgressBar());
            fenetreImport.reactiverBoutons();
        });

        rafraichissementWorker.start();
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
