package analyzor.vue.gestionformat;

import analyzor.controleur.ControleurFormat;
import analyzor.vue.donnees.DTOFormat;
import analyzor.vue.FenetrePrincipale;

import javax.swing.*;
import java.awt.*;

public class FenetreFormat extends JDialog {
    private final DTOFormat DTOFormat;
    private final ControleurFormat controleur;
    private PanneauLignesCalcul panneauLignesCalcul;
    private PanneauLignesInfos panneauLignesInfos;
    private PanneauAjoutFormat panneauAjoutFormat;
    private PanneauBoutons panneauBoutons;
    private boolean modePrecedentEdition;
    public FenetreFormat(FenetrePrincipale fenetrePrincipale, ControleurFormat controleur, DTOFormat DTOFormat) {
        super(fenetrePrincipale, "EUREKA POKER - Gestion des formats", true);
        this.controleur = controleur;
        this.DTOFormat = DTOFormat;
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        ImageIcon iconeImage = new ImageIcon("icon_eureka.png");
        this.setIconImage(iconeImage.getImage());
        inialiserPanneaux();
    }

    /**
     * on crée la structure globale de la page
     */
    private void inialiserPanneaux() {
        JPanel panneauGlobal = new JPanel();
        panneauGlobal.setLayout(new BorderLayout());

        JPanel panneauHaut = new JPanel();
        panneauHaut.setLayout(new BoxLayout(panneauHaut, BoxLayout.X_AXIS));
        panneauLignesInfos = new PanneauLignesInfos(this, this.controleur);
        panneauHaut.add(panneauLignesInfos);
        panneauHaut.add(new JSeparator(JSeparator.VERTICAL));
        panneauLignesCalcul = new PanneauLignesCalcul(this, this.controleur);
        panneauHaut.add(panneauLignesCalcul);
        panneauGlobal.add(panneauHaut, BorderLayout.NORTH);

        panneauGlobal.add(new JSeparator(JSeparator.HORIZONTAL));

        JPanel panneauBas = new JPanel();
        panneauBas.setLayout(new BorderLayout());
        panneauAjoutFormat = new PanneauAjoutFormat(controleur);
        panneauBas.add(panneauAjoutFormat, BorderLayout.WEST);
        panneauBoutons = new PanneauBoutons(this);
        panneauBas.add(panneauBoutons, BorderLayout.SOUTH);
        panneauGlobal.add(panneauBas, BorderLayout.SOUTH);

        this.add(panneauGlobal);
        this.pack();
    }

    /**
     * va regarder tous les éléments modifiés dans DAO et va actualiser l'affichage
     * IMPORTANT => une fois consulté, les éléments modifiés ne sont plus référencés
     * garantit que les deux panneaux sont identiques
     */
    public void actualiser() {
        panneauBoutons.setBoutons();
        for (DTOFormat.InfosFormat infosFormat : DTOFormat.nouveauxFormats()) {
            panneauLignesInfos.ajouterLigne(infosFormat);
            panneauLignesCalcul.ajouterLigne(infosFormat);
        }
        for (DTOFormat.InfosFormat infosFormat : DTOFormat.formatModifies()) {
            panneauLignesInfos.modifierLigne(infosFormat);
            panneauLignesCalcul.modifierLigne(infosFormat);
        }
        for (int indexLigne : DTOFormat.formatsSupprimes()) {
            panneauLignesInfos.supprimerLigne(indexLigne);
            panneauLignesCalcul.supprimerLigne(indexLigne);
        }
        panneauLignesInfos.lignesFinies();
        panneauLignesCalcul.lignesFinies();
        this.pack();
    }

    /**
     * appelé par le controleur selon le mode voulu avant de rendre visible
     * ou bien par les boutons de la page
     */
    public void setModeSelection(boolean active) {
        // si true
        if (active) {
            setModeEdition(false);
            // bouton ligne = bouton sélection (activé)
            // changement de couleurs
            panneauLignesInfos.modeSelection();
            modePrecedentEdition = false;
        }
        // si false désactiver bouton sélection
        else {
            panneauLignesInfos.desactiverBoutons();
        }
        panneauBoutons.setBoutons();
    }

    public void setModeEdition(boolean active) {
        // si true
        // bouton ligne = bouton supprimer (activé)
        // changement de couleurs
        if (active) {
            panneauLignesInfos.modeEdition();
            panneauAjoutFormat.setEtat(true);
            modePrecedentEdition = true;
        }

        // si false bouton ligne = bouton sélection
        // on désactive panneauNouveauFormat
        else {
            panneauLignesInfos.modeSelection();
            panneauLignesInfos.desactiverBoutons();
            panneauAjoutFormat.setEtat(false);
        }
        panneauBoutons.setBoutons();
    }

    /**
     * réservé à panneau LignesCalcul
     */
    protected void setModeCalcul(boolean active) {
        if (active) {
            // va apparaître bouton sélection désactiver
            setModeEdition(false);
            setModeSelection(false);
            panneauBoutons.setActif(false);
        }
        // on réactive le bon mode précédent
        else {
            if (modePrecedentEdition) {
                setModeEdition(true);
            }
            else {
                setModeSelection(true);
            }
            panneauBoutons.setActif(true);
        }
    }

    public void desactiverVue() {
        this.setVisible(false);
    }

    protected boolean isModeEdition() {
        return modePrecedentEdition;
    }

    // appelé lorsqu'on lance un calcul
    // on désactive toute fermeture fenêtre
    protected void toutDesactiver() {
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.panneauBoutons.setFermeture(false);
    }

    protected void toutReactiver() {
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.panneauBoutons.setFermeture(true);
    }
}
