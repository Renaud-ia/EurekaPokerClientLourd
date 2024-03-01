package analyzor.vue.reutilisables.fenetres;

import analyzor.vue.reutilisables.fenetres.FenetreEnfant;

import javax.swing.*;

public abstract class FenetreTroisiemeOrdre extends FenetreEnfant {
    public FenetreTroisiemeOrdre(JDialog fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, false);
        fenetreParente.setVisible(false);
    }
}
