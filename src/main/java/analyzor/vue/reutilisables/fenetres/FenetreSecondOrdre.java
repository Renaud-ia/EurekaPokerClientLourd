package analyzor.vue.reutilisables.fenetres;

import analyzor.vue.reutilisables.fenetres.FenetreEnfant;

import javax.swing.*;


public abstract class FenetreSecondOrdre extends FenetreEnfant {
    protected final JFrame fenetreParente;
    public FenetreSecondOrdre(JFrame fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, modal);
        this.fenetreParente = fenetreParente;
    }

    public void afficher() {
        recentrer();
        this.setVisible(true);
    }

    public void recentrer() {
        this.setLocation(fenetreParente.getLocation().x + DECALAGE_HORIZONTAL, fenetreParente.getLocation().y + DECALAGE_VERTICAL);
    }


}
