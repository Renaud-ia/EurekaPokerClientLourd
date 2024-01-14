package analyzor.controleur;

import analyzor.modele.auth.Utilisateur;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.bdd.ConnexionBDD;
import analyzor.vue.FenetrePrincipale;
import analyzor.vue.vues.VueTaches;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * point d'entrée de l'application
 * gère les opérations de démarrage
 * gère le lancement des différents contrôleurs
 */
public class ControleurPrincipal {
    List<ControleurSecondaire> controleurs = new ArrayList<>();
    List<WorkerAffichable> workers = new ArrayList<>();
    private FenetrePrincipale fenetrePrincipale; // Ajout d'un champ pour la vue principale
    private ControleurTable controleurTable;
    private final VueTaches vueTache = new VueTaches(this);
    

    public static void main(String[] args) {
        // on initialise la BDD + on vérifie que ça marche
        Session session = ConnexionBDD.ouvrirSession();
        ConnexionBDD.fermerSession(session);
        ControleurPrincipal controleur = new ControleurPrincipal();
        controleur.demarrer();
    }

    public void demarrer() {
        FlatLightLaf.setup();

        Font newFont = new Font("Arial", Font.PLAIN, 12);
        FontUIResource fontUIResource = new FontUIResource(newFont);
        UIManager.put("Label.font", fontUIResource);
        UIManager.put("Button.font", fontUIResource);

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        }
        catch (Exception e) {

        }

        fenetrePrincipale = new FenetrePrincipale(this);
        Utilisateur utilisateur = new Utilisateur();
        if (utilisateur.estAuthentifie()) {
            afficherTable();
        }
    }

    public void afficherTable() {
        controleurTable = new ControleurTable(fenetrePrincipale, this);
        lancerControleur(controleurTable);
    }

    public void gererFormats() {
        ControleurSecondaire controleur = new ControleurFormat(fenetrePrincipale, this);
        lancerControleur(controleur);
    }

    public void gererRooms() {
        ControleurSecondaire controleurRoom = new ControleurRoom(fenetrePrincipale, this);
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
        controleurTable.lancerVue();
    }

    public void desactiverVues() {
        for (ControleurSecondaire controleur : this.controleurs) {
            controleur.desactiverVue();
        }
        //todo : est ce que controleur accueil devrait être à part ??
        controleurTable.desactiverVue();
    }

    public void ajouterTache(WorkerAffichable tache) {
        vueTache.ajouterWorker(tache);
    }

    public void formatSelectionne(FormatSolution formatSolution) {
        controleurTable.formatSelectionne(formatSolution);
        controleurTable.lancerVue();
    }
}