package analyzor.vue.gestionformat;

import analyzor.controleur.ControleurFormat;
import analyzor.vue.donnees.DTOFormat;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class PanneauLignesInfos extends PanneauActualisable {
    private ControleurFormat controleur;
    private HashMap<Integer, LigneInfo> lignesInfo;
    private Dimension tailleMinimum = DimensionsFormat.taillePanneauInfos;
    private JLabel aucunFormat = new JLabel("         Aucun format d\u00E9tect\u00E9, ajoutez un format avec le mode \u00E9dition");

    protected PanneauLignesInfos(FenetreFormat fenetreParente, ControleurFormat controleur) {
        super(fenetreParente);
        this.controleur = controleur;
        lignesInfo = new HashMap<>();
    }

    @Override
    protected void ajouterLigne(DTOFormat.InfosFormat infosFormat) {
        LigneInfo nouvelleLigne = new LigneInfo(this, infosFormat);
        lignesInfo.put(infosFormat.getIndexAffichage(), nouvelleLigne);
        this.add(nouvelleLigne);
        super.ajouterEspace();
        this.repaint();
    }

    @Override
    protected void modifierLigne(DTOFormat.InfosFormat infosFormat) {
        LigneInfo ligneModifiee = lignesInfo.get(infosFormat.getIndexAffichage());
        ligneModifiee.actualiser(infosFormat.getNombreParties());
        this.repaint();
    }

    @Override
    protected void supprimerLigne(int index) {
        LigneInfo ligneSupprimee = lignesInfo.get(index);
        this.remove(ligneSupprimee);
        lignesInfo.remove(index);
        lignesFinies();
        this.repaint();
    }

    // appelé par le panneau central
    protected void modeEdition() {
        for (LigneInfo ligne : lignesInfo.values()) {
            ligne.setBoutons(false);
        }
    }

    protected void modeSelection() {
        for (LigneInfo ligne : lignesInfo.values()) {
            ligne.setBoutons(true);
        }
    }

    // on désactive tous les boutons
    public void desactiverBoutons() {
        for (LigneInfo ligne : lignesInfo.values()) {
            ligne.desactiverBouton();
        }
    }

    protected void lignesFinies() {
        if (lignesInfo.isEmpty() && !composantPresent(aucunFormat)) {
            aucunFormat.setPreferredSize(new Dimension(800, 30));
            this.add(aucunFormat);
        }
        else if (!lignesInfo.isEmpty() && composantPresent(aucunFormat)) {
            this.remove(aucunFormat);
        }
    }

    private boolean composantPresent(Component recherche) {
        for (Component composant : getComponents()) {
            if (composant == recherche) return true;
        }
        return false;
    }

    public void formatSelectionne(Long idBDD) {
        controleur.formatSelectionne(idBDD);
    }

    public void demandeSuppressionLigne(Long idBDD, int indexAffichage) {
        int response = JOptionPane.showConfirmDialog(null,
                "Voulez-vous vraiment supprimer ce format?",
                "Suppression", JOptionPane.YES_NO_CANCEL_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            controleur.supprimerFormat(idBDD, indexAffichage);
        }
    }
}
