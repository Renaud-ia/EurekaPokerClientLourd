package analyzor.controleur;

import analyzor.modele.extraction.ControleGestionnaire;
import analyzor.modele.extraction.GestionnaireWinamax;
import analyzor.vue.donnees.InfosRoom;
import analyzor.vue.vues.VueGestionRoom;
import analyzor.vue.vues.VuePrincipale;
import analyzor.vue.vues.VueRooms;

import java.nio.file.Path;

public class ControleurRoom implements ControleurSecondaire {
    /*
    Controle de l'import et de la visualisation des rooms
    controle deux vues différentes : vue globale des rooms et vue spécifique permettant l'import
     */
    //todo : il faut ajouter les gestionnaires qu'on prend en charge ici
    private final ControleGestionnaire[] gestionnaires = {GestionnaireWinamax.obtenir()};
    private int roomSelectionnee;
    private final VueRooms vueRooms;
    private final VueGestionRoom vueGestionRoom;
    private final InfosRoom infosRoom;
    private final ControleurPrincipal controleurPrincipal;
    private boolean gestionActive = false;
    ControleurRoom(VuePrincipale vuePrincipale, ControleurPrincipal controleurPrincipal) {
        this.controleurPrincipal = controleurPrincipal;
        this.infosRoom = new InfosRoom(gestionnaires.length);
        this.vueRooms = new VueRooms(vuePrincipale, this, infosRoom);
        this.vueGestionRoom = new VueGestionRoom(vueRooms, this, infosRoom);
        }
    @Override
    public void demarrer() {
        construireTableDonnees();
        vueRooms.actualiser();
    }

    private void construireTableDonnees() {
        int index = 0;
        for(ControleGestionnaire gestionnaire : gestionnaires) {
            infosRoom.setRoom(index,
                    gestionnaire.getNomRoom(),
                    gestionnaire.nombreMains(),
                    gestionnaire.getConfiguration()
            );
            //todo ajouter les dossiers
            String[] nomDossiers = gestionnaire.getDossiers();
            for (String nomDossier : nomDossiers) {
                int nombreFichiers = gestionnaire.fichiersParDossier(nomDossier);
                infosRoom.ajouterDossier(index, nomDossier, nombreFichiers);
            }
            //todo : test!!! à enlever
            infosRoom.ajouterDossier(0, "C://PARTIES", 56);
            infosRoom.ajouterDossier(0, "C://PARTIES_BIS//NOMTRESLONGPOURVOIRCEQUE_CA_FAIT//ENCORE UN PEU", 123);
            index++;
        }
    }

    public void roomSelectionnee(int index) {
        //todo il faudrait désactiver le bouton
        if (index == -1) return;
        // on garde ça en mémoire pour modifier la bonne room
        this.roomSelectionnee = index;
        this.vueGestionRoom.actualiser(index);
    }

    public void detection() {
        if (gestionnaires[roomSelectionnee].autoDetection()) {
            construireTableDonnees();
            actualiserVues();
        }
        else {
            vueGestionRoom.messageInfo("Aucun nouveau dossier trouvé");
        }
    }

    public void ajouterDossier(Path nomDossier) {
        if (gestionnaires[roomSelectionnee].ajouterDossier(nomDossier)) {
            construireTableDonnees();
            actualiserVues();
            vueGestionRoom.messageInfo("Dossier ajouté avec succès");
        }
        else {
            vueGestionRoom.messageErreur("Le dossier n'a pas pu être ajouté");
        }
    }

    public void supprimerDossier(int ligneSelectionnee) {
        String cheminDossier = infosRoom.getDossiers(roomSelectionnee)[ligneSelectionnee];
        if (gestionnaires[roomSelectionnee].supprimerDossier(cheminDossier)) {
            construireTableDonnees();
            actualiserVues();
            vueGestionRoom.messageInfo("Dossier supprimé");
        }
        else {
            vueGestionRoom.messageErreur("Le dossier n'a pas pu être supprimé");
        }
    }

    public void importer() {
        WorkerAffichable tache = gestionnaires[roomSelectionnee].importer();
        if (tache == null) {
            vueGestionRoom.messageInfo("Aucun dossier à importer");
            return;
        }
        controleurPrincipal.ajouterTache(tache);
        controleurPrincipal.lancerTableWorkers();
    }

    private void actualiserVues() {
        vueRooms.actualiser();
        vueGestionRoom.actualiser(roomSelectionnee);
    }

    @Override
    public void lancerVue() {
        vueRooms.setVisible(true);
        if (gestionActive) vueGestionRoom.setVisible(true);
    }

    @Override
    public void desactiverVue() {
        vueRooms.setVisible(false);
        if (vueGestionRoom.isVisible()) {
            vueGestionRoom.setVisible(false);
            gestionActive = true;
        }
        else {
            // en cas de fermeture par le controleur central
            // on garde l'état de la fenêtre pour savoir si on doit la réafficher
            gestionActive = false;
        }
    }
}
