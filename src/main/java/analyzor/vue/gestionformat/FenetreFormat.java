package analyzor.vue.gestionformat;

import analyzor.controleur.ControleurFormat;
import analyzor.vue.donnees.DAOFormat;
import analyzor.vue.vues.VuePrincipale;

import javax.swing.*;
import java.awt.*;

public class FenetreFormat extends JDialog {
    private final DAOFormat daoFormat;
    private final ControleurFormat controleur;
    private PanneauLignesCalcul panneauLignesCalcul;
    private PanneauLignesInfos panneauLignesInfos;
    private PanneauAjoutFormat panneauAjoutFormat;
    private PanneauBoutons panneauBoutons;
    private boolean modePrecedentEdition;
    public FenetreFormat(VuePrincipale vuePrincipale, ControleurFormat controleur, DAOFormat daoFormat) {
        super(vuePrincipale, "Gestion des formats", true);
        this.controleur = controleur;
        this.daoFormat = daoFormat;
        this.setResizable(false);
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
        panneauLignesInfos = new PanneauLignesInfos(this);
        panneauHaut.add(panneauLignesInfos);
        panneauHaut.add(new JSeparator(JSeparator.VERTICAL));
        panneauLignesCalcul = new PanneauLignesCalcul(this);
        panneauHaut.add(panneauLignesCalcul);
        panneauGlobal.add(panneauHaut, BorderLayout.NORTH);

        panneauGlobal.add(new JSeparator(JSeparator.HORIZONTAL));

        JPanel panneauBas = new JPanel();
        panneauBas.setLayout(new BorderLayout());
        panneauAjoutFormat = new PanneauAjoutFormat(controleur);
        panneauBas.add(panneauAjoutFormat, BorderLayout.WEST);
        panneauBoutons = new PanneauBoutons(this);
        panneauBas.add(panneauBoutons, BorderLayout.EAST);
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
        for (DAOFormat.InfosFormat infosFormat : daoFormat.nouveauxFormats()) {
            panneauLignesInfos.ajouterLigne(infosFormat);
            panneauLignesCalcul.ajouterLigne(infosFormat);
        }
        for (DAOFormat.InfosFormat infosFormat : daoFormat.formatModifies()) {
            panneauLignesInfos.modifierLigne(infosFormat);
            panneauLignesCalcul.modifierLigne(infosFormat);
        }
        for (int indexLigne : daoFormat.formatsSupprimes()) {
            panneauLignesInfos.supprimerLigne(indexLigne);
            panneauLignesCalcul.supprimerLigne(indexLigne);
        }
        this.pack();
    }

    /**
     * appelé par le controleur selon le mode voulu avant de rendre visible
     * ou bien par les boutons de la page
     */
    public void setModeSelection(boolean active) {
        // si true
        if (active) {
            System.out.println("Mode sélection activé");
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

    }

    /**
     * réservé à panneau LignesCalcul
     */
    protected void setModeCalcul(boolean active) {
        if (active) {
            // va apparaître bouton sélection désactiver
            setModeEdition(false);
            setModeSelection(false);
        }
        // on réactive le bon mode précédent
        else {
            if (modePrecedentEdition) {
                setModeSelection(true);
            }
            else {
                setModeEdition(true);
            }
        }
    }

    public void desactiverVue() {
        this.setVisible(false);
    }

    protected boolean isModeEdition() {
        return modePrecedentEdition;
    }

}
