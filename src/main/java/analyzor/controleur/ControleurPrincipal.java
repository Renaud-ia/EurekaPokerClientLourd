package analyzor.controleur;

import analyzor.modele.auth.Utilisateur;
import analyzor.modele.parties.RequetesBDD;
import analyzor.vue.vues.VuePrincipale;
import analyzor.vue.vues.VueTaches;
import com.formdev.flatlaf.FlatLightLaf;

import java.util.ArrayList;
import java.util.List;

public class ControleurPrincipal {
    List<ControleurSecondaire> controleurs = new ArrayList<>();
    List<WorkerAffichable> workers = new ArrayList<>();
    private VuePrincipale vuePrincipale; // Ajout d'un champ pour la vue principale
    private ControleurAccueil controleurAccueil;
    private final VueTaches vueTache = new VueTaches(this);
    

    public static void main(String[] args) {
        // on initialise la BDD + on vérifie que ça marche
        RequetesBDD.ouvrirSession();
        RequetesBDD.fermerSession();
        ControleurPrincipal controleur = new ControleurPrincipal();
        controleur.demarrer();
    }

    public void demarrer() {
        FlatLightLaf.setup();
        vuePrincipale = new VuePrincipale(this);
        Utilisateur utilisateur = new Utilisateur();
        if (utilisateur.estAuthentifie()) {
            afficherTable();
        }
    }

    public void afficherTable() {
        controleurAccueil = new ControleurAccueil(vuePrincipale);
    }

    public void gererRooms() {
        ControleurSecondaire controleurRoom = new ControleurRoom(vuePrincipale, this);
        lancerControleur(controleurRoom);
    }

    public void fermeture() {
        // Actions de fermeture de l'application
        this.vueTache.dispose();
    }

    private void lancerControleur(ControleurSecondaire controleurAjoute) {
        // vérifie que le controleur n'est pas déjà lancé
        for (ControleurSecondaire controleur : this.controleurs) {
            if (controleur.getClass() == controleurAjoute.getClass()) {
                controleur.lancerVue();
                return;
            }
        }
        this.controleurs.add(controleurAjoute);
        controleurAjoute.demarrer();
    }

    public void lancerTableWorkers() {
        vueTache.afficher();
    }

    public void reactiverVues() {
        controleurAccueil.lancerVue();
    }

    public void desactiverVues() {
        for (ControleurSecondaire controleur : this.controleurs) {
            controleur.desactiverVue();
        }
        //todo : est ce que controleur accueil devrait être à part ??
        controleurAccueil.desactiverVue();
    }

    public void ajouterTache(WorkerAffichable tache) {
        vueTache.ajouterWorker(tache);
    }
}