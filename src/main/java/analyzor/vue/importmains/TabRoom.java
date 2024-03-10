package analyzor.vue.importmains;

import analyzor.controleur.ControleurRoom;
import analyzor.vue.basiques.Images;
import analyzor.vue.donnees.rooms.InfosRoom;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Objects;

public class TabRoom extends JPanel implements ActionListener, ListSelectionListener {
    private static final int N_LIGNES_DOSSIERS_MIN = 5;
    private final ControleurRoom controleurRoom;
    private final InfosRoom infosRoom;
    private JPanel panelDossiers;
    private DefaultTableModel listeDossiers;
    private JTable listeSelectionnable;
    private JButton supprimerDossier;
    private JButton ajouterDossier;
    private JButton autoDetection;
    private JLabel nombreFichiersImportes;
    private JLabel nombreMainsImportees;
    private JLabel erreursImport;
    private JButton voirLogs;
    TabRoom(ControleurRoom controleurRoom, InfosRoom infosRoom) {
        this.controleurRoom = controleurRoom;
        this.infosRoom = infosRoom;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        construirePanneaux();
        actualiser();
    }

    // méthodes publique de contrôle par le controleur

    public void actualiser() {
        // on ajoute les dossiers
        listeDossiers.setRowCount(0);
        for (String nomDossier : infosRoom.getDossiers()) {
            listeDossiers.addRow(new String[] {nomDossier});
        }

        nombreFichiersImportes.setText(infosRoom.getNombreFichiersImportes());
        nombreMainsImportees.setText(infosRoom.getNombreMainsImportees());
        erreursImport.setText(infosRoom.getNombreErreursImport());

        panelDossiers.repaint();
        this.revalidate();
        this.repaint();
    }


    // méthodes privées

    private void construirePanneaux() {
        // panneau pour les dossiers
        panelDossiers = new JPanel();
        panelDossiers.setLayout(new BoxLayout(panelDossiers, BoxLayout.Y_AXIS));
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Dossiers");
        EmptyBorder bordureInterne = new EmptyBorder(5, 5, 5, 5);
        CompoundBorder bordureComposee = new CompoundBorder(titledBorder, bordureInterne);
        panelDossiers.setBorder(bordureComposee);

        listeDossiers = new DefaultTableModel(0, 1);
        listeSelectionnable = new JTable(listeDossiers);
        listeSelectionnable.setTableHeader(null);
        JScrollPane scrollPane = new JScrollPane(listeSelectionnable);
        scrollPane.setPreferredSize(new Dimension(450, 150));
        listeSelectionnable.getSelectionModel().addListSelectionListener(this);

        panelDossiers.add(scrollPane);

        JPanel boutonsDossiers = new JPanel(new FlowLayout());

        ajouterDossier = new JButton("Ajouter");
        ajouterDossier.setIcon(new ImageIcon(Images.ajouterDossier));
        ajouterDossier.addActionListener(this);
        boutonsDossiers.add(ajouterDossier);

        supprimerDossier = new JButton("Supprimer");
        supprimerDossier.setIcon(new ImageIcon(Images.supprimerDossier));
        supprimerDossier.addActionListener(this);
        boutonsDossiers.add(supprimerDossier);
        supprimerDossier.setEnabled(false);

        autoDetection = new JButton("Détecter");
        autoDetection.setIcon(new ImageIcon(Images.detection));
        autoDetection.addActionListener(this);
        boutonsDossiers.add(autoDetection);
        panelDossiers.add(boutonsDossiers);

        this.add(panelDossiers);

        // infos sur la room

        // nombre de fichiers importés
        JPanel fichiersImportes = new JPanel();
        fichiersImportes.setLayout(new FlowLayout());
        JLabel labelFichiersImportes = new JLabel("Nombre de fichiers importés : ");
        fichiersImportes.add(labelFichiersImportes);
        nombreFichiersImportes = new JLabel(infosRoom.getNombreFichiersImportes());
        fichiersImportes.add(nombreFichiersImportes);
        this.add(fichiersImportes);

        // nombre de mains importés
        JPanel mainsImportes = new JPanel();
        mainsImportes.setLayout(new FlowLayout());
        JLabel labelMainsImportees = new JLabel("Nombre de mains importées : ");
        mainsImportes.add(labelMainsImportees);
        nombreMainsImportees = new JLabel(infosRoom.getNombreMainsImportees());
        mainsImportes.add(nombreMainsImportees);
        this.add(mainsImportes);


        // nombre de fichiers bugués
        JPanel importsRates = new JPanel();
        JLabel labelImportsRates = new JLabel("Nombre de fichiers non importés : ");
        importsRates.add(labelImportsRates);
        erreursImport = new JLabel(infosRoom.getNombreErreursImport());
        importsRates.add(erreursImport);
        voirLogs = new JButton("Voir les logs");
        voirLogs.addActionListener(this);
        importsRates.add(voirLogs);
        this.add(importsRates);

        this.repaint();
        this.revalidate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ajouterDossier) {
            // ouvrir une fenêtre de recherche
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = fileChooser.getSelectedFile();
                String cheminDossier = selectedDirectory.getAbsolutePath();
                controleurRoom.ajouterDossier(this.infosRoom, cheminDossier);
            }

        }

        else if (e.getSource() == supprimerDossier) {
            int rangeeSelectionnee = listeSelectionnable.getSelectedRow();
            String valeurSelectionnee = (String) listeSelectionnable.getValueAt(rangeeSelectionnee, 0);
            if (Objects.equals(valeurSelectionnee, "-")) return;
            // récupérer la valeur de la JList
            controleurRoom.supprimerDossier(this.infosRoom, valeurSelectionnee);
        }

        else if (e.getSource() == autoDetection) {
            controleurRoom.detection(infosRoom);
        }

        else if (e.getSource() == voirLogs) {
            controleurRoom.afficherLogs(infosRoom);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int rangeeSelectionnee = listeSelectionnable.getSelectedRow();
            String valeurSelectionnee = (String) listeSelectionnable.getValueAt(rangeeSelectionnee, 0);
            System.out.println(valeurSelectionnee);
            supprimerDossier.setEnabled(valeurSelectionnee != null);
        }
    }
}
