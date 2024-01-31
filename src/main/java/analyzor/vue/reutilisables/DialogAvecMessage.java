package analyzor.vue.reutilisables;

import javax.swing.*;

public class DialogAvecMessage extends JDialog {
    public DialogAvecMessage(JDialog fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, modal);
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
