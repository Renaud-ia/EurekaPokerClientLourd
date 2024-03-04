package analyzor.vue.reutilisables.fenetres;

import analyzor.vue.basiques.Polices;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FenetreChargement extends JDialog {
    private final JProgressBar progressBar;
    private final int MARGE_INTERNE = 30;
    public FenetreChargement(JFrame frame, String texte) {
        super(frame, "", true);
        JPanel panneauContenu = new JPanel();
        panneauContenu.setLayout(new BoxLayout(panneauContenu, BoxLayout.Y_AXIS));
        EmptyBorder bordureInterne = new EmptyBorder(MARGE_INTERNE, MARGE_INTERNE, MARGE_INTERNE, MARGE_INTERNE);
        panneauContenu.setBorder(bordureInterne);

        JLabel labelTexte = new JLabel(texte);
        labelTexte.setFont(Polices.standard);
        panneauContenu.add(labelTexte);

        panneauContenu.add(Box.createRigidArea(new Dimension(0, 10)));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);
        panneauContenu.add(progressBar);

        this.add(panneauContenu);
        this.setUndecorated(true);
        this.pack();
        this.setLocationRelativeTo(frame);
    }

    public void lancer() {
        this.setVisible(true);
    }

    public void arreter() {
        this.dispose();
    }
}
