package analyzor.vue.gestionformat;

import analyzor.vue.donnees.DAOFormat;

import javax.swing.*;

public abstract class PanneauActualisable extends JPanel {
    final FenetreFormat fenetreParente;

    protected PanneauActualisable(FenetreFormat fenetreParente) {
        this.fenetreParente = fenetreParente;
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    protected abstract void ajouterLigne(DAOFormat.InfosFormat infosFormat);
    protected abstract void modifierLigne(DAOFormat.InfosFormat infosFormat);
    protected abstract void supprimerLigne(int index);

    protected void ajouterEspace() {
        // réglage de l'espace entre les lignes
        this.add(Box.createVerticalStrut(1));
    }
}
