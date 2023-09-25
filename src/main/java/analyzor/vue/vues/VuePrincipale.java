package analyzor.vue.vues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VuePrincipale extends JFrame {
    private int largeurEcran ;
    private int hauteurEcran ;
    public VuePrincipale() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        largeurEcran = (int) (screenSize.width * 0.9);
        hauteurEcran = (int) (screenSize.height * 0.9);
        setTitle("PokerAnalyzor v0.0");
        setSize(largeurEcran, hauteurEcran);
        setBackground(Color.cyan);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Gérer la fermeture ici (par exemple, demander une confirmation)
                int choix = JOptionPane.showConfirmDialog(null,
                        "Voulez-vous vraiment quitter ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION);

                if (choix == JOptionPane.YES_OPTION) {
                    dispose(); // Fermer la fenêtre
                }
            }
        });

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Désactive la gestion par défaut de la fermeture
    }

    public int getLargeurEcran() {
        return largeurEcran;
    }

    public int getHauteurEcran() {
        return hauteurEcran;
    }


}
