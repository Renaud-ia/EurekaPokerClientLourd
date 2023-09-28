package analyzor.controleur;

import analyzor.modele.auth.Utilisateur;
import analyzor.vue.vues.VuePrincipale;
import analyzor.vue.vues.VueAccueil;

import java.util.ArrayList;
import java.util.List;

public class ControleurPrincipal {
    List<ControleurSecondaire> controleurs = new ArrayList<>();
    private VuePrincipale vuePrincipale; // Ajout d'un champ pour la vue principale
    

    public static void main(String[] args) {
        ControleurPrincipal controleur = new ControleurPrincipal();
        controleur.demarrer();
    }

    public void demarrer() {
        vuePrincipale = new VuePrincipale(this);
        Utilisateur utilisateur = new Utilisateur();
        if (utilisateur.estAuthentifie()) {
            afficherTable();
        }
    }

    public void afficherTable() {
        ControleurAccueil controleurAccueil = new ControleurAccueil(vuePrincipale);
    }

    public void gererRooms() {
        System.out.println("Gestion des rooms déclenchée");
        ControleurSecondaire controleurRoom = new ControleurRoom(vuePrincipale, this);
        lancerControleur(controleurRoom);
    }

    public void fermeture() {
        // Actions de fermeture de l'application
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

    public void ecranProgression(ProgressionTache tache, ControleurSecondaire controleurActif) {
        for (ControleurSecondaire controleur : this.controleurs) {
            controleur.desactiverVue();
        }
        // on lance l'écran progression
        // on l'actualise toutes les x secondes
        //todo que faire si la fenêtre se ferme => on détecte que setVisible = false
        //todo est ce que si on met des sleep ça va pas être le bordel

        // on affiche le message configuré
        // à la fin, on relance le contrôleur actif
        controleurActif.lancerVue();
    }

    public void operationTerminee() {

    }

}