package analyzor.vue;

import analyzor.controleur.ControleurPrincipal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FenetrePrincipale extends JFrame implements ActionListener {
    private final ControleurPrincipal controleur;
    private final int largeurEcran ;
    private final int hauteurEcran ;
    public FenetrePrincipale(ControleurPrincipal controleur) {
        this.controleur = controleur;
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        this.setLayout(new BorderLayout());
        largeurEcran = (int) (screenSize.width * 0.9);
        hauteurEcran = (int) (screenSize.height * 0.9);
        setTitle("EUREKA POKER");
        setSize(largeurEcran, hauteurEcran);
        setLocationRelativeTo(null);
        ImageIcon iconeImage = new ImageIcon("icon_eureka.png");
        this.setIconImage(iconeImage.getImage());
        ajouterMenu();

        // doit être appelé après la création du menu pour éviter les bugs d'afficahge
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
                    controleur.fermeture();
                }
            }
        });

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Désactive la gestion par défaut de la fermeture
    }
    private void ajouterMenu() {
        JMenuBar barreMenus = new JMenuBar();
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

        this.repaint();
    }

    public int getLargeurEcran() {
        return largeurEcran;
    }

    public int getHauteurEcran() {
        return hauteurEcran;
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
}
