package analyzor.vue.reutilisables.fenetres;

import analyzor.vue.reutilisables.fenetres.FenetreEnfant;

import javax.swing.*;

/**
 * classe abstraite qui définit le comportement des fenetres de second ordre
 */
public abstract class FenetreSecondOrdre extends FenetreEnfant {
    public FenetreSecondOrdre(JFrame fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, modal);
    }
}
