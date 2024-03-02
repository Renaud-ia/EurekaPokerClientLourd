package analyzor.vue.reutilisables.fenetres;

import analyzor.vue.basiques.CouleursDeBase;

import javax.swing.*;

/**
 * classe de base pour les fenêtres qui se rajoutent par dessus la fenêtre principale
 */
abstract class FenetreEnfant extends JDialog {
    FenetreEnfant(JFrame fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, modal);
        this.setBackground(CouleursDeBase.FOND_FENETRE);
    }

    FenetreEnfant(JDialog fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, modal);
        this.setBackground(CouleursDeBase.FOND_FENETRE);
    }

    public void messageErreur(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erreur",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void messageInfo(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
