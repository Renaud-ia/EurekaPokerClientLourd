package analyzor.vue;

import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.basiques.Polices;
import analyzor.vue.basiques.Images;
import analyzor.vue.reutilisables.fenetres.FenetreEnfant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class EcranAccueil extends FenetreEnfant implements ActionListener {
    private final FenetrePrincipale fenetrePrincipale;
    private final JPanel panneauContenu;
    private final JLabel labelEtat;
    private final JProgressBar barreProgression;
    private JButton boutonOk;
    public EcranAccueil(FenetrePrincipale fenetrePrincipale) {
        super(fenetrePrincipale, "Demarrage", true);
        this.fenetrePrincipale = fenetrePrincipale;

        this.setBackground(CouleursDeBase.FOND_FENETRE);
        this.setUndecorated(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 

        panneauContenu = new JPanel();
        panneauContenu.setBackground(CouleursDeBase.FOND_FENETRE);
        EmptyBorder bordureInterneGlobale = new EmptyBorder(20, 20, 20, 20);
        panneauContenu.setBorder(bordureInterneGlobale);
        panneauContenu.setLayout(new BoxLayout(panneauContenu, BoxLayout.Y_AXIS));

        JPanel panneauLogo = new JPanel();
        panneauLogo.setLayout(new FlowLayout());
        panneauLogo.add(new JLabel(new ImageIcon(Images.icone)));
        JLabel labelLogo = new JLabel("Eur\u00EAka Poker");
        labelLogo.setFont(Polices.titre);
        labelLogo.setForeground(CouleursDeBase.PANNEAU_FONCE);
        panneauLogo.add(labelLogo);

        panneauContenu.add(panneauLogo);
        panneauContenu.add(Box.createRigidArea(new Dimension(0, 10)));

        labelEtat = new JLabel("D\u00E9marrage en cours");
        labelEtat.setPreferredSize(new Dimension(200, 20));
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

    
    public void arreter() {

    }

    public void termine(String message) {
        labelEtat.setText("D\u00E9marrage termin\u00E9");
        barreProgression.setIndeterminate(false);
        barreProgression.setMaximum(barreProgression.getMaximum());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        boutonOk = new JButton("Ok");
        boutonOk.setAlignmentX(Component.CENTER_ALIGNMENT);
        boutonOk.addActionListener(this);
        panneauContenu.add(boutonOk);
        panneauContenu.revalidate();
        this.pack();
        this.setLocationRelativeTo(fenetrePrincipale);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonOk) dispose();
    }
}
