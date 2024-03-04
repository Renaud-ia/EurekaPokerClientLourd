package analyzor.controleur;

import analyzor.modele.auth.Utilisateur;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.licence.LicenceManager;
import analyzor.vue.EcranAccueil;
import analyzor.vue.FenetrePrincipale;
import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.basiques.Polices;
import com.formdev.flatlaf.FlatIntelliJLaf;
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
    private FenetrePrincipale fenetrePrincipale; // Ajout d'un champ pour la vue principale
    private ControleurTable controleurTable;
    

    public static void main(String[] args) {
        ControleurPrincipal controleur = new ControleurPrincipal();
        controleur.demarrer();
    }

    public void demarrer() {
        FlatLightLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
            FontUIResource fontUIResource = new FontUIResource(Polices.standard);
            UIManager.put("Label.font", fontUIResource);
            UIManager.put("Button.font", fontUIResource);
            UIManager.put("Panel.background", CouleursDeBase.FOND_FENETRE);
            UIManager.put("TitlePane.backgroundColor", CouleursDeBase.FOND_FENETRE);
            UIManager.put("TitledPane.buttonHoverBackground", CouleursDeBase.PANNEAU_FONCE);
        }
        catch (Exception e) {

        }

        fenetrePrincipale = new FenetrePrincipale(this);
        demarrerControleurs();
    }

    private void demarrerControleurs() {
        final EcranAccueil ecranAccueil = new EcranAccueil(fenetrePrincipale);
        SwingUtilities.invokeLater(ecranAccueil::demarrer);

        try {
            ecranAccueil.setMessage("Lancement de la base de données...");
            // on initialise la BDD + on vérifie que ça marche
            Session session = ConnexionBDD.ouvrirSession();
            ConnexionBDD.fermerSession(session);

            ecranAccueil.setMessage("Récupération des imports...");
            ControleurSecondaire controleurRoom = new ControleurRoom(fenetrePrincipale, this);
            lancerControleur(controleurRoom);

            ecranAccueil.setMessage("Chargement des formats...");
            ControleurSecondaire controleur = new ControleurFormat(fenetrePrincipale, this);
            lancerControleur(controleur);

            ecranAccueil.setMessage("Vérification de la licence...");
            ControleurSecondaire controleurLicence = new ControleurLicence(fenetrePrincipale);
            lancerControleur(controleurLicence);

            ecranAccueil.setMessage("Préparation de l'affichage...");
            controleurTable = new ControleurTable(fenetrePrincipale, this);
            lancerControleur(controleurTable);

            fenetrePrincipale.afficher();
            redimensionnerRange();
            if (LicenceManager.getInstance().connexionRatee()) {
                ecranAccueil.messageInfo("La licence n'a pas pu être vérifiée, vérifiez votre connexion");
            }
            ecranAccueil.termine(null);
        }
        catch (Exception e) {
            // todo afficher un message d'erreur
            ecranAccueil.messageErreur("Erreur fatale lors du démarrage");
            ecranAccueil.arreter();
            System.exit(1);
        }
    }

    public void gererFormats() {
        ControleurSecondaire controleur = new ControleurFormat(fenetrePrincipale, this);
        lancerControleur(controleur);
    }

    public void gererRooms() {
        ControleurSecondaire controleurRoom = new ControleurRoom(fenetrePrincipale, this);
        lancerControleur(controleurRoom);
    }

    public void gererLicence() {
        ControleurSecondaire controleurLicence = new ControleurLicence(fenetrePrincipale);
        lancerControleur(controleurLicence);
    }

    public void fermeture() {
        // besoin car sinon ne s'arrête pas
        System.exit(0);
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

    public void formatSelectionne(FormatSolution formatSolution) {
        controleurTable.formatSelectionne(formatSolution);
        controleurTable.lancerVue();
    }

    public void redimensionnerRange() {
        if (controleurTable != null) controleurTable.redimensionnerRange();
    }
}