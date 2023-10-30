package analyzor.vue.gestionformat;

import analyzor.vue.donnees.DAOFormat;

import javax.swing.*;
import java.awt.*;

public class LigneCalcul extends JPanel {
    private final PanneauLignesCalcul panneauParent;
    private final CardLayout cardLayout;
    private final DAOFormat.InfosFormat infosFormat;
    InfoCalcul ligneInfoCalcul;
    LancerCalcul ligneLancerCalcul;
    // doit avoir accès à son panneau
    // CardLayout avec deux panneaux différents pour mode editionCalcul et mode vueCalcul
    protected LigneCalcul(PanneauLignesCalcul panneauParent, DAOFormat.InfosFormat infosFormat) {
        this.panneauParent = panneauParent;
        this.infosFormat = infosFormat;
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);
        construireVueLigne();
        setModeActif(false);
    }

    private void construireVueLigne() {
        ligneInfoCalcul = new InfoCalcul(this, infosFormat.getEtat());
        this.add(ligneInfoCalcul, "Info");

        ligneLancerCalcul = new LancerCalcul(this,
                infosFormat.isPreflopCalcule(),
                infosFormat.isFlopCalcule());
        this.add(ligneLancerCalcul, "Lancer");
    }

    protected void clicCalculer() {
        // TODO : il faut avoir accès au contrôleur
        // on bascule la vue de cette ligne
        // on va désactiver mode sélection et mode édition
        panneauParent.setModeCalcul(true, this);
        setModeActif(true);
    }

    protected void clicOk() {
        // on bascule la vue de cette ligne
        // on rétablit mode sélection
        panneauParent.setModeCalcul(false, this);
        setModeActif(false);
    }

    protected void clickReinitialiser() {
    }

    /**
     * est ce que le mode calcul est activé pour cette ligne
     * @param active
     */
    public void setModeActif(boolean active) {
        // on switch sur le panneau correspondant de CardLayout
        if (active) {
            cardLayout.show(this, "Lancer");
        }
        else {
            cardLayout.show(this, "Info");
        }
    }

    public void setBoutonCalculer(boolean active) {
        ligneInfoCalcul.setBoutonCalculer(active);
    }

    public void actualiser() {
        ligneInfoCalcul.changerEtat(infosFormat.getEtat());
    }
}
