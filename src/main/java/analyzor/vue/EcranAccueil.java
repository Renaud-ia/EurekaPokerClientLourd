package analyzor.vue;

import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.basiques.Polices;
import analyzor.vue.basiques.Images;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * page de chargement lorsqu'on lance le logiciel
 */
public class EcranAccueil extends JDialog {
    private final JLabel labelEtat;
    private final JProgressBar barreProgression;
    private JButton boutonOk;
    public EcranAccueil(FenetrePrincipale fenetrePrincipale) {
        super(fenetrePrincipale, "Demarrage", true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Désactive la gestion par défaut de la fermeture

        JPanel panneauContenu = new JPanel();
        EmptyBorder bordureInterneGlobale = new EmptyBorder(10, 10, 10, 10);
        panneauContenu.setBorder(bordureInterneGlobale);
        panneauContenu.setLayout(new BoxLayout(panneauContenu, BoxLayout.Y_AXIS));

        JPanel panneauLogo = new JPanel();
        panneauLogo.setLayout(new FlowLayout());
        panneauLogo.add(new JLabel(new ImageIcon(Images.icone)));
        JLabel labelLogo = new JLabel("Eurêka Poker");
        labelLogo.setFont(Polices.titre);
        labelLogo.setBackground(CouleursDeBase.PANNEAU_FONCE);
        panneauLogo.add(labelLogo);

        panneauContenu.add(panneauLogo);
        panneauContenu.add(Box.createRigidArea(new Dimension(0, 10)));

        labelEtat = new JLabel("Démarrage en cours");
        labelEtat.setPreferredSize(new Dimension(200, 10));
        panneauContenu.add(labelEtat);
        panneauContenu.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel panelProgression = new JPanel();
        EmptyBorder bordureInterneProgression = new EmptyBorder(10, 10, 10, 10);
        panelProgression.setBorder(bordureInterneProgression);
        panelProgression.setLayout(new BorderLayout());
        barreProgression = new JProgressBar();
        barreProgression.setIndeterminate(true);
        barreProgression.setStringPainted(false);
        barreProgression.setMaximumSize(new Dimension(200, 5));

        panelProgression.add(barreProgression, BorderLayout.CENTER);
        panneauContenu.add(panelProgression);

        this.add(panneauContenu);

        this.pack();
        this.setLocationRelativeTo(fenetrePrincipale);
    }

    public void demarrer() {
        this.setVisible(true);
    }

    public void setMessage(String message) {
        labelEtat.setText(message);
        labelEtat.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    /**
     * la fenêtre ne sera jamais réutilisée
     */
    public void arreter() {

    }

    public void termine(String message) {
        labelEtat.setText("Démarrage terminé");
        barreProgression.setIndeterminate(false);
        barreProgression.setMaximum(barreProgression.getMaximum());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
