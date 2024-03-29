package analyzor.vue.reutilisables.fenetres;

import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.table.CadreBandeau;

import javax.swing.*;
import java.awt.*;


public abstract class FenetreEnfant extends JDialog {
    protected static final int DECALAGE_HORIZONTAL = 100;
    protected static final int DECALAGE_VERTICAL = CadreBandeau.hauteur + 80;
    protected FenetreEnfant(JFrame fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, modal);
        this.setUndecorated(false);
        this.setBackground(CouleursDeBase.FOND_FENETRE);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    protected FenetreEnfant(JDialog fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, modal);
        this.setBackground(CouleursDeBase.FOND_FENETRE);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
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

    public void desactiverBoutons() {
        for (Component component : getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(false);
            }
        }
    }

    public void reactiverBoutons() {
        for (Component component : getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(true);
            }
        }
    }

    public void desactiverControles() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        desactiverBoutons();
    }

    public void reactiverControles() {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        reactiverBoutons();
    }
}
