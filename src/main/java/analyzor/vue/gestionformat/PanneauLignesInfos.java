package analyzor.vue.gestionformat;

import analyzor.vue.donnees.DAOFormat;

import java.awt.*;
import java.util.HashMap;

public class PanneauLignesInfos extends PanneauActualisable {
    private HashMap<Integer, LigneInfo> lignesInfo;
    private Dimension tailleMinimum = DimensionsFormat.taillePanneauInfos;

    protected PanneauLignesInfos(FenetreFormat fenetreParente) {
        super(fenetreParente);
        this.setSize(300, 200);
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

    }

    protected void modeSelection() {

    }

    // on désactive tous les boutons
    public void desactiverBoutons() {
    }

    @Override
    public Dimension getMinimumSize() {
        return tailleMinimum;
    }

    @Override
    public Dimension getPreferredSize() {
        return tailleMinimum;
    }
}
