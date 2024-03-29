package analyzor.vue;

import analyzor.controleur.ControleurPrincipal;
import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.basiques.Images;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

public class FenetrePrincipale extends JFrame implements ActionListener {
    private final static int MIN_LARGEUR = 1100;
    private final static int MIN_HAUTEUR = 700;
    private final ControleurPrincipal controleur;

    private JMenuItem quitter;
    private JMenuItem gestionRooms;
    private JMenuItem gestionFormats;
    private JMenuItem licence;
    public FenetrePrincipale(ControleurPrincipal controleur) {
        this.controleur = controleur;

        this.setLayout(new BorderLayout());
        setTitle("EUR\u00CAKA POKER");
        setLocationRelativeTo(null);
        this.setIconImage(Images.icone);
        this.setResizable(true);
        this.setBackground(CouleursDeBase.FOND_FENETRE);
        this.setMinimumSize(new Dimension(MIN_LARGEUR, MIN_HAUTEUR));
        this.setSize(new Dimension(MIN_LARGEUR, MIN_HAUTEUR));
        ajouterMenu();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                demanderFermeture();
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                controleur.redimensionnerRange();
            }
        });

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    private void ajouterMenu() {
        JMenuBar barreMenus = new JMenuBar();
        barreMenus.setBackground(CouleursDeBase.FOND_FENETRE);
        this.setJMenuBar(barreMenus);

        JMenu menuFichier = new JMenu("Fichier");
        barreMenus.add(menuFichier);
        quitter = new JMenuItem("Quitter");
        menuFichier.add(quitter);
        quitter.addActionListener(this);

        JMenu menuGestion = new JMenu("Gestion");
        barreMenus.add(menuGestion);
        gestionRooms = new JMenuItem("Imports");
        menuGestion.add(gestionRooms);
        gestionRooms.addActionListener(this);

        gestionFormats = new JMenuItem("Formats");
        menuGestion.add(gestionFormats);
        gestionFormats.addActionListener(this);

        JMenu menuOutils = new JMenu("Outils");
        barreMenus.add(menuOutils);
        licence = new JMenuItem("Licence");
        menuOutils.add(licence);
        licence.addActionListener(this);
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == quitter) demanderFermeture();
        else if (e.getSource() == gestionRooms) this.controleur.gererRooms();
        else if (e.getSource() == gestionFormats) this.controleur.gererFormats();
        else if (e.getSource() == licence) controleur.gererLicence();

    }

    public void afficher() {
        this.setSize(MIN_LARGEUR, MIN_HAUTEUR);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void demanderFermeture() {

        int choix = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment quitter ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (choix == JOptionPane.YES_OPTION) {
            dispose();
            controleur.fermeture();
        }
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
