package analyzor.vue.vues;

import analyzor.controleur.ControleurRoom;
import analyzor.vue.composants.TableNonModifiable;
import analyzor.vue.donnees.InfosRoom;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VueRooms extends JDialog implements ActionListener {
    private DefaultTableModel model;
    private JTable tableau;
    private final int largeurFenetre;
    private final int hauteurFenetre;
    private final ControleurRoom controleur;
    public VueRooms(VuePrincipale frame, ControleurRoom controleurRoom) {
        super(frame, "Gestion des rooms", true);
        this.controleur = controleurRoom;
        this.setLayout(new BorderLayout());
        largeurFenetre = (int) (frame.getLargeurEcran() * 0.5);
        hauteurFenetre = (int) (frame.getHauteurEcran() * 0.5);
        setSize(largeurFenetre, hauteurFenetre);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Fermez la JDialog lors de la fermeture
        setLocationRelativeTo(null);
        afficherRooms();
        setVisible(true);
    }

    public void afficherRooms() {
        this.model = new TableNonModifiable();

        this.model.addColumn("Site");
        this.model.addColumn("Parties");
        this.model.addColumn("Mains");
        this.model.addColumn("Dossiers");
        this.model.addColumn("Etat");

        model.addRow(new Object[]{"Winamax", "Test", "Test", "Test", "Test"});
        model.addRow(new Object[]{"Winamax", "Test", "Test", "Test", "Test"});
        model.addRow(new Object[]{"Winamax", "Test", "Test", "Test", "Test"});

        /*
        for (int i=0; i<infosRoom.nRooms(); i++) {
            Object[] donneesLigne = infosRoom.getDonneesRooms(i);
            model.addRow(donneesLigne);
        }
        */

        //todo : faire notre propre modèle de tableau pour personnaliser les cellules
        tableau = new JTable(this.model);
        JTableHeader header = tableau.getTableHeader();
        header.setReorderingAllowed(false);

        ListSelectionModel selectionModel = new DefaultListSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableau.setSelectionModel(selectionModel);

        // flow Layout affiche mal les tableaux
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(largeurFenetre, (int) (hauteurFenetre * 0.5)));

        panel.add(tableau.getTableHeader(), BorderLayout.NORTH);
        panel.add(tableau, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(100, 30));
        JButton bouton = new JButton("Configurer");
        bouton.addActionListener(this);
        buttonPanel.add(bouton);
        panel.add(buttonPanel, BorderLayout.EAST);

        this.add(panel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int ligneSelectionne = tableau.getSelectedRow();
        controleur.bugVue();
        controleur.roomSelectionnee(ligneSelectionne);
    }

    public void modifierRoom(int indexLigne) {
        model.setValueAt("Nouvelle valeur", indexLigne, 0);
    }
}
