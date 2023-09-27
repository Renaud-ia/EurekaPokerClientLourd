package analyzor.controleur;

import analyzor.modele.auth.Utilisateur;
import analyzor.vue.vues.VuePrincipale;
import analyzor.vue.vues.VueAccueil;

public class ControleurPrincipal {
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
        ControleurRoom controleurRoom = new ControleurRoom(vuePrincipale);
    }

    public void fermeture() {
        // Actions de fermeture de l'application
    }

}