package analyzor.vue.importmains;

import analyzor.controleur.ControleurRoom;
import analyzor.vue.FenetrePrincipale;
import analyzor.vue.basiques.Images;
import analyzor.vue.donnees.rooms.InfosRoom;
import analyzor.vue.reutilisables.fenetres.FenetreSecondOrdre;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class FenetreImport extends FenetreSecondOrdre implements ActionListener {
    private final ControleurRoom controleurRoom;
    private final FenetrePrincipale fenetrePrincipale;
    private List<TabRoom> rooms;
    private JTabbedPane tabsRooms;
    private JPanel panneauImport;
    private JButton boutonRafraichir;
    private JButton boutonLancer;
    private JButton boutonStop;


    public FenetreImport(ControleurRoom controleurRoom, FenetrePrincipale fenetrePrincipale) {
        super(fenetrePrincipale, "Import de mains", true);
        this.controleurRoom = controleurRoom;
        this.fenetrePrincipale = fenetrePrincipale;

        this.setResizable(false);
        this.setLocationRelativeTo(fenetrePrincipale);
        ImageIcon iconeImage = new ImageIcon("icon_eureka.png");
        this.setIconImage(iconeImage.getImage());

        construireFenetre();
        this.rooms = new ArrayList<>();
    }

    private void construireFenetre() {
        JPanel panneauContenu = new JPanel();
        panneauContenu.setLayout(new BoxLayout(panneauContenu, BoxLayout.Y_AXIS));

        tabsRooms = new JTabbedPane();
        panneauContenu.add(tabsRooms);

        panneauContenu.add(Box.createRigidArea(new Dimension(0, 10)));
        panneauContenu.add(new JSeparator(SwingConstants.HORIZONTAL));

        panneauImport = new JPanel();
        panneauImport.setLayout(new FlowLayout());
        panneauContenu.add(panneauImport);

        this.add(panneauContenu);

        // on construit les boutons mais on ne les affiche pas
        boutonRafraichir = new JButton("Rafraichir");
        boutonRafraichir.setIcon(new ImageIcon(Images.rafraichir));
        boutonRafraichir.addActionListener(this);
        boutonLancer = new JButton("Lancer");
        boutonLancer.setIcon(new ImageIcon(Images.lancerImport));
        boutonLancer.addActionListener(this);
        boutonStop = new JButton("Pause");
        boutonStop.addActionListener(this);
    }

    // méthodes du controleur

    public void ajouterProgressBar(JProgressBar progressBar) {
        panneauImport.removeAll();
        panneauImport.add(boutonRafraichir);
        progressBar.setStringPainted(true);
        progressBar.setVisible(true);
        progressBar.setPreferredSize(new Dimension(180, 22));
        panneauImport.add(progressBar);
        panneauImport.add(boutonLancer);

        this.repaint();
        this.revalidate();

        this.pack();
    }

    public void ajouterRoom(InfosRoom room) {
        TabRoom tabRoom = new TabRoom(controleurRoom, room);
        this.tabsRooms.addTab(room.getNom(), room.getIcone(), tabRoom);
        this.rooms.add(tabRoom);

        this.revalidate();
        this.repaint();

        fenetrePrincipale.pack();
    }

    public void rafraichirDonnees() {
        for (TabRoom tabRoom : rooms) {
            tabRoom.actualiser();
        }

        fenetrePrincipale.pack();
    }


    // gestion des évènements

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonRafraichir) {
            controleurRoom.rafraichirWorker();
        }

        // c'est un worker donc pas besoin du contrôleur
        else if (e.getSource() == boutonLancer) {
            panneauImport.remove(boutonLancer);
            panneauImport.add(boutonStop);
            this.repaint();
            this.revalidate();
            controleurRoom.lancerWorker();
        }

        else if (e.getSource() == boutonStop) {
            controleurRoom.arreterWorker();
        }
    }


}
