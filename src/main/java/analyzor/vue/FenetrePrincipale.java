package analyzor.vue;

import analyzor.controleur.ControleurPrincipal;
import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.basiques.Images;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FenetrePrincipale extends JFrame implements ActionListener {
    private final static int MIN_LARGEUR = 1100;
    private final static int MIN_HAUTEUR = 700;
    private final ControleurPrincipal controleur;
    public FenetrePrincipale(ControleurPrincipal controleur) {
        this.controleur = controleur;

        this.setLayout(new BorderLayout());
        setTitle("EUREKA POKER");
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
                // Gérer la fermeture ici (par exemple, demander une confirmation)
                int choix = JOptionPane.showConfirmDialog(e.getComponent(),
                        "Voulez-vous vraiment quitter ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION);

                if (choix == JOptionPane.YES_OPTION) {
                    dispose(); // Fermer la fenêtre
                    controleur.fermeture();
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                controleur.redimensionnerRange();
            }
        });

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Désactive la gestion par défaut de la fermeture
    }
    private void ajouterMenu() {
        JMenuBar barreMenus = new JMenuBar();
        barreMenus.setBackground(CouleursDeBase.FOND_FENETRE);
        this.setJMenuBar(barreMenus);

        JMenu menuFichier = new JMenu("Fichier");
        barreMenus.add(menuFichier);

        JMenu menuImport = new JMenu("Import");
        barreMenus.add(menuImport);
        JMenuItem rooms = new JMenuItem("Gestion des rooms");
        menuImport.add(rooms);
        rooms.addActionListener(this);

        JMenu menuFormat = new JMenu("Format");
        barreMenus.add(menuFormat);
        JMenuItem gestionFormat = new JMenuItem("Gerer les formats");
        menuFormat.add(gestionFormat);
        gestionFormat.addActionListener(this);

        JMenu menuOutils = new JMenu("Outils");
        barreMenus.add(menuOutils);
        JMenuItem gestionLicence = new JMenuItem("Gestion de la licence");
        menuOutils.add(gestionLicence);
        gestionLicence.addActionListener(this);
    }


    //gestion des menus de la fenêtre
    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        System.out.println(actionCommand);
        if (actionCommand.equals("Gestion des rooms")) this.controleur.gererRooms();
        else if (actionCommand.equals("Gerer les formats")) this.controleur.gererFormats();
        else if (actionCommand.equals("Gestion de la licence")) controleur.gererLicence();

    }

    public void afficher() {
        this.setSize(MIN_LARGEUR, MIN_HAUTEUR);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
