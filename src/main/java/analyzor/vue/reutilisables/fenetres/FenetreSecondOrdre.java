package analyzor.vue.reutilisables.fenetres;

import analyzor.vue.reutilisables.fenetres.FenetreEnfant;

import javax.swing.*;

/**
 * classe abstraite qui définit le comportement des fenetres de second ordre
 */
public abstract class FenetreSecondOrdre extends FenetreEnfant {
    protected final JFrame fenetreParente;
    public FenetreSecondOrdre(JFrame fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, modal);
        this.fenetreParente = fenetreParente;
    }

    public void afficher() {
        this.setLocationRelativeTo(fenetreParente);
        this.setVisible(true);
    }
}
