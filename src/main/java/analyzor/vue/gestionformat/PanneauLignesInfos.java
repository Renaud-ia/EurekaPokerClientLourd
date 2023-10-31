package analyzor.vue.gestionformat;

import analyzor.vue.donnees.DAOFormat;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class PanneauLignesInfos extends PanneauActualisable {
    private HashMap<Integer, LigneInfo> lignesInfo;
    private Dimension tailleMinimum = DimensionsFormat.taillePanneauInfos;
    private JLabel aucunFormat = new JLabel("         Aucun format d\u00E9tect\u00E9, ajoutez un format avec le mode \u00E9dition");

    protected PanneauLignesInfos(FenetreFormat fenetreParente) {
        super(fenetreParente);
        lignesInfo = new HashMap<>();
        aucunFormat.setPreferredSize(new Dimension(800, 30));
        this.add(aucunFormat);
    }

    @Override
    protected void ajouterLigne(DAOFormat.InfosFormat infosFormat) {
        LigneInfo nouvelleLigne = new LigneInfo(this, infosFormat);
        lignesInfo.put(infosFormat.getIndexAffichage(), nouvelleLigne);
        this.add(nouvelleLigne);
        super.ajouterEspace();
        this.repaint();
    }

    @Override
    protected void modifierLigne(DAOFormat.InfosFormat infosFormat) {
        this.repaint();
    }

    @Override
    protected void supprimerLigne(int index) {
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
        if (!lignesInfo.isEmpty() && composantPresent(aucunFormat)) {
            this.remove(aucunFormat);
        }
    }

    private boolean composantPresent(Component recherche) {
        for (Component composant : getComponents()) {
            if (composant == recherche) return true;
        }
        return false;
    }
}
