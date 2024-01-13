package analyzor.vue.vues;

import analyzor.controleur.ControleurRoom;
import analyzor.vue.couleurs.CouleursDeBase;
import analyzor.vue.reutilisables.CadreLarge;
import analyzor.vue.reutilisables.TableNonModifiable;
import analyzor.vue.donnees.InfosRoom;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VueGestionRoom extends JDialog implements ActionListener {
    private final ControleurRoom controleur;
    private final InfosRoom infosRoom;
    private JTable tableau;
    private final JPanel panneauFenetre = new JPanel();
    private JButton boutonDetection;
    private JButton boutonAjouter;
    private JButton boutonSupprimer;
    private JButton boutonImporter;

    public VueGestionRoom(VueRooms vueRooms, ControleurRoom controleur, InfosRoom infosRoom) {
        super(vueRooms, "Gestion de la room", false);
        this.controleur = controleur;
        this.infosRoom = infosRoom;
        setSize((int) (vueRooms.getLargeurFenetre() * 0.9), (int) (vueRooms.getHauteurFenetre() * 0.7));
        parametrerFenetre();
    }

    private void parametrerFenetre() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(CouleursDeBase.FOND_FONCE);
        configurerBoutons();
    }

    private void configurerBoutons() {
        //todo importer des icones
        boutonDetection = new JButton("Detection");
        boutonAjouter = new JButton("Ajouter");
        boutonSupprimer = new JButton("Supprimer");
        boutonImporter = new JButton("Importer");

        boutonDetection.addActionListener(this);
        boutonAjouter.addActionListener(this);
        boutonSupprimer.addActionListener(this);
        boutonImporter.addActionListener(this);
    }

    public void actualiser(int indexRoom) {
        panneauFenetre.setBackground(CouleursDeBase.FOND_FONCE);
        panneauFenetre.removeAll();
        panneauFenetre.setLayout(new BoxLayout(panneauFenetre, BoxLayout.Y_AXIS));

        CadreLarge cadreLabels = new CadreLarge();
        cadreLabels.setLayout(new BoxLayout(cadreLabels, BoxLayout.Y_AXIS));
        JLabel labelRoom = new JLabel("Configuration de : " + infosRoom.nomRoom(indexRoom));
        JLabel labelEtat = new JLabel("Etat : " + infosRoom.etatRoom(indexRoom));
        cadreLabels.add(labelRoom);
        cadreLabels.add(labelEtat);
        cadreLabels.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panneauFenetre.add(cadreLabels);

        JPanel table = dessinerTable(indexRoom);
        table.setBackground(CouleursDeBase.FOND_CLAIR);
        panneauFenetre.add(table);

        CadreLarge cadreBoutons = new CadreLarge();
        cadreBoutons.setLayout(new FlowLayout());
        ajouterBoutons(cadreBoutons);
        panneauFenetre.add(cadreBoutons);

        this.add(panneauFenetre);
        this.setVisible(true);
    }

    private JPanel dessinerTable(int indexRoom) {
        JPanel panneauTable = new CadreLarge();
        panneauTable.setLayout(new BorderLayout());

        TableNonModifiable model = new TableNonModifiable();
        model.addColumn("Chemin du dossier");
        model.addColumn("Nombre de fichiers");


        String[] nomsDossier = infosRoom.getDossiers(indexRoom);
        for (String dossier: nomsDossier) {
            int nParties = infosRoom.getNParties(indexRoom, dossier);
            model.addRow(new Object[]{dossier, nParties});
        }

        tableau = new JTable(model);

        JTableHeader header = tableau.getTableHeader();
        header.setReorderingAllowed(false);

        ListSelectionModel selectionModel = new DefaultListSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableau.setSelectionModel(selectionModel);

        panneauTable.add(tableau.getTableHeader(), BorderLayout.NORTH);
        panneauTable.add(tableau, BorderLayout.CENTER);

        return panneauTable;
    }

    private void ajouterBoutons(JPanel cadreBoutons) {
        cadreBoutons.add(boutonDetection);
        cadreBoutons.add(boutonAjouter);
        cadreBoutons.add(boutonSupprimer);
        cadreBoutons.add(boutonImporter);
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


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonImporter) {
            controleur.importer();
        }
        if (e.getSource() == boutonAjouter) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = fileChooser.getSelectedFile();
                String cheminDossier = selectedDirectory.getAbsolutePath();
                Path cheminPath = Paths.get(cheminDossier);
                controleur.ajouterDossier(cheminPath);
            }

        }
        if (e.getSource() == boutonSupprimer) {
            int ligneSelectionnee = tableau.getSelectedRow();
            if (ligneSelectionnee == -1) return;
            controleur.supprimerDossier(ligneSelectionnee);
        }
        if (e.getSource() == boutonDetection) {
            controleur.detection();
        }
    }

}
